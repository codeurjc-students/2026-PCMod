package es.codeurjcstudents.pcmod.e2e.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.io.InputStream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@Tag("server-system")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
        .body("message", equalTo("message: Bad credentials, path: /api/v1/auth/login"));
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

  @Test
  public void getOwnImage() throws IOException {
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

    byte[] expectedImage;
    try (InputStream imageResource = getClass().getResourceAsStream("/sample_images/user.png")) {
      expectedImage = imageResource.readAllBytes();
    }

    given()
        .cookie("AuthToken", authToken)
        .multiPart("imageFile", "user.png", expectedImage, "image/png")
        .when().post("users/1/image")
        .then().statusCode(201).contentType(ContentType.JSON);

    byte[] actualImage = given()
        .header("Content-Type", "application/json")
        .cookie("AuthToken", authToken)
        .when().get("users/1/image")
        .then().statusCode(200).contentType(MediaType.IMAGE_JPEG_VALUE)
        .extract().asByteArray();

    assertArrayEquals(expectedImage, actualImage);
  }

  @Test
  public void getOwnVoidImage() throws IOException {
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

    byte[] expectedImage;
    try (InputStream imageResource = getClass().getResourceAsStream("/sample_images/empty-profile-image.jpg")) {
      expectedImage = imageResource.readAllBytes();
    }

    byte[] actualImage = given()
        .header("Content-Type", "application/json")
        .cookie("AuthToken", authToken)
        .when().get("users/1/image")
        .then().statusCode(200).contentType(MediaType.IMAGE_JPEG_VALUE)
        .extract().asByteArray();

    assertArrayEquals(expectedImage, actualImage);
  }

  @Test
  public void getOtherUserImageAsNoAdmin() {
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
        .header("Content-Type", "application/json")
        .cookie("AuthToken", authToken)
        .when().get("users/2/image")
        .then().statusCode(403).contentType(ContentType.JSON)
        .body("status", equalTo(403))
        .body("error", equalTo("Forbidden"));
  }

  @Test
  public void getImagesAsAdmin() {
    String authToken = given()
        .header("Content-Type", "application/json")
        .body("""
            {
                "username": "admin@example.com",
                "password": "adminpass"
            }
            """)
        .when().post("auth/login")
        .then().statusCode(200).contentType(ContentType.JSON)
        .extract().cookie("AuthToken");

    given()
        .header("Content-Type", "application/json")
        .cookie("AuthToken", authToken)
        .when().get("users/1/image")
        .then().statusCode(200).contentType(MediaType.IMAGE_JPEG_VALUE);

    given()
        .header("Content-Type", "application/json")
        .cookie("AuthToken", authToken)
        .when().get("users/2/image")
        .then().statusCode(200).contentType(MediaType.IMAGE_JPEG_VALUE);
  }

  @Test
  public void register() {
    given()
        .header("Content-Type", "application/json")
        .body("""
            {
              "name": "register",
              "surname": "user",
              "username": "register.user",
              "address" : "c/ example 3",
              "email": "register.user@example.com",
              "password": "R3gister_user_pass"
            }
            """)
        .when().post("auth/register")
        .then().statusCode(201).contentType(ContentType.JSON)
        .body("id", notNullValue())
        .body("name", equalTo("register"))
        .body("surname", equalTo("user"))
        .body("username", equalTo("register.user"))
        .body("address", equalTo("c/ example 3"))
        .body("email", equalTo("register.user@example.com"))
        .body("roles[0]", equalTo("REGISTERED_USER"));
  }

  @Test
  public void registerVoidCredentials() {
    given()
        .header("Content-Type", "application/json")
        .body("""
            {
              "name": "",
              "surname": "",
              "username": "",
              "address" : "",
              "email": "",
              "password": ""
            }
            """)
        .when().post("auth/register")
        .then().statusCode(500).contentType(ContentType.JSON)
        .body("status", equalTo(500))
        .body("message", equalTo(
            "Validation failed: [Name is required, Surname is required, Username is required, Email is required, Password is required]"));
  }

  @Test
  public void registerNullCredentials() {
    given()
        .header("Content-Type", "application/json")
        .body("""
            {
              "name": null,
              "surname": null,
              "username": null,
              "address" : null,
              "email": null,
              "password": null
            }
            """)
        .when().post("auth/register")
        .then().statusCode(500).contentType(ContentType.JSON)
        .body("status", equalTo(500))
        .body("message", equalTo(
            "Validation failed: [Name is required, Surname is required, Username is required, Email is required, Password is required]"));
  }

  @Test
  public void registerExistingCredentials() {
    given()
        .header("Content-Type", "application/json")
        .body("""
            {
              "name": "user",
              "surname": "example",
              "username": "user_example",
              "address" : "c/example_address 1",
              "email": "user@example.com",
              "password": "R3gister_user_pass"
            }
            """)
        .when().post("auth/register")
        .then().statusCode(500).contentType(ContentType.JSON)
        .body("status", equalTo(500))
        .body("message", equalTo("Validation failed: [Username already exists, Email already exists]"));
  }

  @Test
  public void registerBadFormatCredentials() {
    given()
        .header("Content-Type", "application/json")
        .body("""
            {
              "name": "registerUser",
              "surname": "example",
              "username": "register.user.example",
              "address" : "c/example_address 5",
              "email": "test@email",
              "password": "pass"
            }
            """)
        .when().post("auth/register")
        .then().statusCode(500).contentType(ContentType.JSON)
        .body("status", equalTo(500))
        .body("message", equalTo(
            "Validation failed: [Email format is invalid. It should be in the format: example@domain.com, Password must contain at least 8 characters including 1 uppercase letter, 1 lowercase letter, 1 digit, and 1 special character (@$!%*?&-_)]"));
  }

  @Test
  public void postOwnImage() throws IOException {
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

    byte[] expectedImage;
    try (InputStream imageResource = getClass().getResourceAsStream("/sample_images/user.png")) {
      expectedImage = imageResource.readAllBytes();
    }

    given()
        .cookie("AuthToken", authToken)
        .multiPart("imageFile", "user.png", expectedImage, "image/png")
        .when().post("users/1/image")
        .then().statusCode(201).contentType(ContentType.JSON);
  }

  @Test
  public void postOtherUserImageAsNoAdmin() throws IOException {
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

    byte[] expectedImage;
    try (InputStream imageResource = getClass().getResourceAsStream("/sample_images/user.png")) {
      expectedImage = imageResource.readAllBytes();
    }

    given()
        .cookie("AuthToken", authToken)
        .multiPart("imageFile", "user.png", expectedImage, "image/png")
        .when().post("users/2/image")
        .then().statusCode(403).contentType(ContentType.JSON)
        .body("status", equalTo(403))
        .body("error", equalTo("Forbidden"));
  }

  @Test
  public void postImagesAsAdmin() throws IOException {
    String authToken = given()
        .header("Content-Type", "application/json")
        .body("""
            {
                "username": "admin@example.com",
                "password": "adminpass"
            }
            """)
        .when().post("auth/login")
        .then().statusCode(200).contentType(ContentType.JSON)
        .extract().cookie("AuthToken");

    byte[] expectedImage;
    try (InputStream imageResource = getClass().getResourceAsStream("/sample_images/user.png")) {
      expectedImage = imageResource.readAllBytes();
    }

    given()
        .cookie("AuthToken", authToken)
        .multiPart("imageFile", "user.png", expectedImage, "image/png")
        .when().post("users/1/image")
        .then().statusCode(201).contentType(ContentType.JSON);

    given()
        .cookie("AuthToken", authToken)
        .multiPart("imageFile", "user.png", expectedImage, "image/png")
        .when().post("users/2/image")
        .then().statusCode(201).contentType(ContentType.JSON);
  }

}
