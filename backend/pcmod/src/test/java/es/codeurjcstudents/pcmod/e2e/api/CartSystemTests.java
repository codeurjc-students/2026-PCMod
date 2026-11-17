package es.codeurjcstudents.pcmod.e2e.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Tag("server-system")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CartSystemTests {

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
  public void getCartAsNoLoggedIn() {
    given().header("Content-Type", "application/json")
      .when().get("cart/")
      .then().statusCode(401).contentType(ContentType.JSON)
      .body("error", equalTo("Unauthorized"));
  }

  @Test
  public void getCartAsLoggedIn() {

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
    .extract().cookie("AuthToken");
    
    given()
      .cookie("AuthToken", authToken)
      .header("Content-Type", "application/json")
      .when().get("cart/")
      .then().statusCode(200).contentType(ContentType.JSON)
      .body("items", hasSize(2));
  }
  
}
