package es.codeurjcstudents.pcmod.e2e.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Tag("server-system")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UsersSystemTests {

  @LocalServerPort
  private int port;

  @BeforeEach
  public void setup() {
    RestAssured.port = port;
    RestAssured.baseURI = "https://localhost";
    RestAssured.basePath = "/api/v1/";
    RestAssured.useRelaxedHTTPSValidation();
  }

  @Test
  public void login() {
    given()
        .header("Content-Type", "application/json")
        .body("""
            {
                "username": "user@example.com",
                "password": "userpass"
            }
            """)
        .when().post("auth/login")
        .then().statusCode(200).contentType(ContentType.JSON)
        .body("error", nullValue())
        .body("message", equalTo("Auth successful. Tokens are created in cookie."))
        .body("status", equalTo("SUCCESS"))
        .cookie("AuthToken", notNullValue())
        .cookie("RefreshToken", notNullValue());
  }

  @Test
  public void loginError() {
    given()
        .header("Content-Type", "application/json")
        .body("""
            {
                "username": "user@example.com",
                "password": "pass"
            }
            """)
        .when().post("auth/login")
        .then().statusCode(401).contentType(ContentType.JSON)
        .body("error", equalTo("Unauthorized"))
        .body("status", equalTo(401))
        .body("message", nullValue());
  }

  @Test
  public void refresh() {
    String refreshToken = given()
        .header("Content-Type", "application/json")
        .body("""
            {
              "username": "user@example.com",
              "password": "userpass"
            }
            """)
        .when().post("auth/login")
        .then().statusCode(200)
        .extract().cookie("RefreshToken");

    given()
        .cookie("RefreshToken", refreshToken)
        .when().post("auth/refresh")
        .then().statusCode(200).contentType(ContentType.JSON)
        .body("error", nullValue())
        .body("message", equalTo("Auth successful. Tokens are created in cookie."))
        .body("status", equalTo("SUCCESS"));
  }

  @Test
  public void logout() {
    given().header("Content-Type", "application/json")
        .when().post("auth/logout")
        .then().statusCode(200).contentType(ContentType.JSON)
        .body("error", nullValue())
        .body("message", equalTo("Logout successfully"))
        .body("status", equalTo("SUCCESS"))
        .cookie("AuthToken", emptyString())
        .cookie("RefreshToken", emptyString());
  }

  @Test
  public void getMe() {
        String authToken = given()
        .header("Content-Type", "application/json")
        .body("""
            {
                "username": "user@example.com",
                "password": "userpass"
            }
            """)
        .when().post("auth/login")
        .then().statusCode(200).contentType(ContentType.JSON)
        .body("error", nullValue())
        .body("message", equalTo("Auth successful. Tokens are created in cookie."))
        .body("status", equalTo("SUCCESS"))
        .cookie("AuthToken", notNullValue())
        .cookie("RefreshToken", notNullValue())
        .extract().cookie("AuthToken");

    given()
        .header("Content-Type", "application/json")
        .cookie("AuthToken", authToken)
        .when().get("users/me")
        .then().statusCode(200).contentType(ContentType.JSON)
        .body("id", equalTo(1))
        .body("name", equalTo("user"))
        .body("surname", equalTo("example"))
        .body("username", equalTo("user_example"))
        .body("email", equalTo("user@example.com"))
        .body("address", equalTo("c/example_address 1"))
        .body("roles[0]", equalTo("REGISTERED_USER"));
  }

}
