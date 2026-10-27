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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.io.InputStream;

@Tag("server-system")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ComponentsSystemTests {

  @LocalServerPort
  private int port;

  @BeforeEach
  public void setup() {
    RestAssured.port = port;
    RestAssured.baseURI = "https://localhost";
    RestAssured.basePath = "/api/v1";
    RestAssured.useRelaxedHTTPSValidation();
  }

  @Test
  public void getComponents() {
    given().header("Content-Type", "application/json")
        .when().get("/components/")
        .then().statusCode(200).contentType(ContentType.JSON)
        // Global checks
        .body("totalElements", greaterThan(10))
        .body("content", hasSize(greaterThan(10)))
        .body("content.brand",
            hasItems("AMD", "Gigabyte", "Corsair", "MSI", "Kingston", "Asus", "Seagate", "Lian Li", "Intel"))
        .body("content.price", everyItem(greaterThanOrEqualTo(0.0f)))
        .body("content.stock", everyItem(greaterThanOrEqualTo(0)))
        // Specific checks (e.g., First Component)
        .body("content[0].id", notNullValue())
        .body("content[0].name", equalTo("AMD Ryzen 7 7800X3D"))
        .body("content[0].type", equalTo("CPU"))
        .body("content[0].brand", equalTo("AMD"))
        .body("content[0].price", equalTo(350.0f))
        .body("content[0].stock", equalTo(5));
  }

  @Test
  public void getComponent() {
    given().header("Content-Type", "application/json")
        .when().get("/components/1")
        .then().statusCode(200).contentType(ContentType.JSON)
        .body("id", notNullValue())
        .body("name", equalTo("AMD Ryzen 7 7800X3D"))
        .body("type", equalTo("CPU"))
        .body("brand", equalTo("AMD"))
        .body("price", equalTo(350.0f))
        .body("stock", equalTo(5));
  }

  @Test
  public void getComponentNotFound() {
    given().header("Content-Type", "application/json")
        .when().get("/components/13")
        .then().statusCode(404).contentType(ContentType.JSON)
        .body("error", equalTo("Not Found"))
        .body("message", equalTo("Componente no encontrado."));
  }

  @Test
  public void getComponentImage() throws IOException {
    byte[] expectedImage;
    try (InputStream imageResource = getClass().getResourceAsStream("/sample_images/ryzen-7.webp")) {
      expectedImage = imageResource.readAllBytes();
    }

    byte[] actualImage = given().header("Content-Type", "application/json")
        .when().get("/components/1/image")
        .then().statusCode(200).contentType("image/jpeg")
        .extract().asByteArray();

    assertArrayEquals(expectedImage, actualImage);
  }

  @Test
  public void deleteComponentAsNotLoggedIn() {

    given()
        .when().delete("components/1")
        .then().statusCode(401).contentType(ContentType.JSON)
        .body("status", equalTo(401))
        .body("error", equalTo("Unauthorized"));
  }

  @Test
  public void deleteComponentAsLoggedInNoAdmin() {
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
        .when().delete("components/1")
        .then().statusCode(403).contentType(ContentType.JSON)
        .body("status", equalTo(403))
        .body("error", equalTo("Forbidden"));
  }

  @Test
  public void deleteComponentAsAdmin() {
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
        .when().delete("components/1")
        .then().statusCode(200);
  }

}
