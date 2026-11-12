package es.codeurjcstudents.pcmod.e2e.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.io.InputStream;

@Tag("server-system")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
        // Specific checks without reviews(e.g., First Component)
        .body("content[0].id", notNullValue())
        .body("content[0].name", equalTo("AMD Ryzen 7 7800X3D"))
        .body("content[0].type", equalTo("CPU"))
        .body("content[0].brand", equalTo("AMD"))
        .body("content[0].price", equalTo(350.0f))
        .body("content[0].stock", equalTo(5))
        .body("content[0].reviews", empty())
        // Specific checks with reviews(e.g., Second Component)
        .body("content[1].id", notNullValue())
        .body("content[1].name", equalTo("NVIDIA GeForce RTX 5060 WINDFORCE MAX OC"))
        .body("content[1].type", equalTo("GPU"))
        .body("content[1].brand", equalTo("Gigabyte"))
        .body("content[1].price", equalTo(359.85f))
        .body("content[1].stock", equalTo(2))
        .body("content[1].reviews", not(empty()));
  }

  @Test
  public void getComponentWithoutReviews() {
    given().header("Content-Type", "application/json")
        .when().get("/components/1")
        .then().statusCode(200).contentType(ContentType.JSON)
        .body("id", notNullValue())
        .body("name", equalTo("AMD Ryzen 7 7800X3D"))
        .body("type", equalTo("CPU"))
        .body("brand", equalTo("AMD"))
        .body("price", equalTo(350.0f))
        .body("stock", equalTo(5))
        .body("reviews", empty());
  }

  @Test
  public void getComponentWithReviews() {
    given().header("Content-Type", "application/json")
        .when().get("/components/2")
        .then().statusCode(200).contentType(ContentType.JSON)
        .body("id", notNullValue())
        .body("name", equalTo("NVIDIA GeForce RTX 5060 WINDFORCE MAX OC"))
        .body("type", equalTo("GPU"))
        .body("brand", equalTo("Gigabyte"))
        .body("price", equalTo(359.85f))
        .body("stock", equalTo(2))
        .body("reviews", not(empty()));
  }

  @Test
  public void getComponentNotFound() {
    given().header("Content-Type", "application/json")
        .when().get("/components/15")
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
  public void postComponentAsNoLoggedIn() {
    given()
        .header("Content-Type", "application/json")
        .body("""
            {
              "name": "AMD Ryzen 5 9600X",
              "description": "El AMD Ryzen 5 9600X destaca por su arquitectura de 4 nm y el nuevo socket AM5.",
              "type" : "CPU",
              "brand" : "AMD",
              "price" : 182.95,
              "stock" : 7
            }
            """)
        .when().post("/components/")
        .then().statusCode(401).contentType(ContentType.JSON)
        .body("error", equalTo("Unauthorized"));
  }

  @Test
  public void postComponentAsRegisteredUserNoAdmin() {
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
        .body("""
            {
              "name": "AMD Ryzen 5 9600X",
              "description": "El AMD Ryzen 5 9600X destaca por su arquitectura de 4 nm y el nuevo socket AM5.",
              "type" : "CPU",
              "brand" : "AMD",
              "price" : 182.95,
              "stock" : 7
            }
            """)
        .when().post("/components/")
        .then().statusCode(403).contentType(ContentType.JSON)
        .body("error", equalTo("Forbidden"));
  }

  @Test
  public void postComponentAsAdmin() {
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
        .cookie("AuthToken", authToken)
        .header("Content-Type", "application/json")
        .body("""
            {
              "name": "AMD Ryzen 5 9600X",
              "description": "El AMD Ryzen 5 9600X destaca por su arquitectura de 4 nm y el nuevo socket AM5.",
              "type" : "CPU",
              "brand" : "AMD",
              "price" : 182.95,
              "stock" : 7
            }
            """)
        .when().post("/components/")
        .then().statusCode(201).contentType(ContentType.JSON)
        .body("id", notNullValue())
        .body("name", equalTo("AMD Ryzen 5 9600X"))
        .body("description", equalTo("El AMD Ryzen 5 9600X destaca por su arquitectura de 4 nm y el nuevo socket AM5."))
        .body("type", equalTo("CPU"))
        .body("brand", equalTo("AMD"))
        .body("price", equalTo(182.95F))
        .body("stock", equalTo(7));
  }

  @Test
  public void postVoidComponentData() {
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
        .cookie("AuthToken", authToken)
        .header("Content-Type", "application/json")
        .body("""
            {
              "name": "",
              "description": "",
              "type": "CPU",
              "brand" : "",
              "price": 182.95,
              "stock": 7
            }
            """)
        .when().post("/components/")
        .then().statusCode(500).contentType(ContentType.JSON)
        .body("message", equalTo(
            "Validación fallida:\n• El nombre del componente es obligatorio.\n• La descripción es obligatoria.\n• La marca es obligatoria."));
  }

  @Test
  public void postNullComponentData() {
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
        .cookie("AuthToken", authToken)
        .header("Content-Type", "application/json")
        .body("""
            {
              "name": null,
              "description": null,
              "type": "CPU",
              "brand" : null,
              "price": 182.95,
              "stock": 7
            }
            """)
        .when().post("/components/")
        .then().statusCode(500).contentType(ContentType.JSON)
        .body("message", equalTo(
            "Validación fallida:\n• El nombre del componente es obligatorio.\n• La descripción es obligatoria.\n• La marca es obligatoria."));
  }

  @Test
  public void postExistingComponentData() {
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
        .cookie("AuthToken", authToken)
        .header("Content-Type", "application/json")
        .body("""
            {
              "name": "Seagate BarraCuda 3.5",
              "description": "Sample description.",
              "type" : "STORAGE",
              "brand" : "Seagate",
              "price" : 100.00,
              "stock" : 2
            }
            """)
        .when().post("/components/")
        .then().statusCode(500).contentType(ContentType.JSON)
        .body("status", equalTo(500))
        .body("message", equalTo("Validación fallida:\n• Ya existe un componente con ese nombre."));
  }

  @Test
  public void postBadNumComponentData() {
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
        .cookie("AuthToken", authToken)
        .header("Content-Type", "application/json")
        .body("""
            {
              "name": "sampleComponent",
              "description": "sampleDescription",
              "type" : "CPU",
              "brand" : "sampleBrand",
              "price" : "-5",
              "stock" : "-5"
            }
            """)
        .when().post("/components/")
        .then().statusCode(500).contentType(ContentType.JSON)
        .body("status", equalTo(500))
        .body("message", equalTo(
            "Validación fallida:\n• El precio del producto ha de ser mayor de 0.00 €.\n• El stock del producto ha de ser 0 o mayor."));
  }

  @Test
  public void postBadFormatComponentData() {
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
        .cookie("AuthToken", authToken)
        .header("Content-Type", "application/json")
        .body("""
            {
              "name": "AMD Ryzen 5 9600X",
              "description": "El AMD Ryzen 5 9600X destaca por su arquitectura de 4 nm y el nuevo socket AM5.",
              "type" : "CPU",
              "brand" : "AMD",
              "price" : "bad_format",
              "stock" : "bad_format"
            }
            """)
        .when().post("/components/")
        .then().statusCode(400).contentType(ContentType.JSON)
        .body("status", equalTo(400))
        .body("message", equalTo(
            "JSON parse error: Cannot deserialize value of type `java.math.BigDecimal` from String \"bad_format\": not a valid representation"));
  }

  @Test
  public void postComponentImageAsNoLoggedIn() throws IOException {
    byte[] expectedImage;
    try (InputStream imageResource = getClass().getResourceAsStream("/sample_images/ryzen-7.webp")) {
      expectedImage = imageResource.readAllBytes();
    }

    given()
        .multiPart("imageFile", "ryzen-7.webp", expectedImage, "image/webp")
        .when().post("components/2/image")
        .then().statusCode(401).contentType(ContentType.JSON)
        .body("error", equalTo("Unauthorized"));
  }

  @Test
  public void postComponentImageAsRegisteredUserNoAdmin() throws IOException {
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
    try (InputStream imageResource = getClass().getResourceAsStream("/sample_images/ryzen-7.webp")) {
      expectedImage = imageResource.readAllBytes();
    }

    given()
        .cookie("AuthToken", authToken)
        .multiPart("imageFile", "ryzen-7.webp", expectedImage, "image/webp")
        .when().post("components/2/image")
        .then().statusCode(403).contentType(ContentType.JSON)
        .body("error", equalTo("Forbidden"));
  }

  @Test
  public void postComponentImageAsAdmin() throws IOException {
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
    try (InputStream imageResource = getClass().getResourceAsStream("/sample_images/ryzen-7.webp")) {
      expectedImage = imageResource.readAllBytes();
    }

    given()
        .cookie("AuthToken", authToken)
        .multiPart("imageFile", "ryzen-7.webp", expectedImage, "image/webp")
        .when().post("components/2/image")
        .then().statusCode(201).contentType(ContentType.JSON);
  }

  @Test
  public void postComponentImageWithInvalidFile() {
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

    byte[] invalidTypeImage = new byte[1024];

    given()
        .cookie("AuthToken", authToken)
        .multiPart("imageFile", "test.txt", invalidTypeImage, "text/plain")
        .when().post("components/2/image")
        .then().statusCode(500).contentType(ContentType.JSON)
        .body("status", equalTo(500))
        .body("message", equalTo("El tipo de archivo no es válido. Solo se permiten imágenes JPEG, PNG y WebP."));
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

  @Test
  public void putComponentAsNoLoggedIn() {

    given()
        .header("Content-Type", "application/json")
        .body("""
            {
              "name": "sampleNamePUT",
              "description": "samepleDescriptionPUT",
              "type" : "CPU",
              "brand" : "sampleBrandPUT",
              "price" : 100.00,
              "stock" : 5
            }
            """)
        .when().put("components/5")
        .then().statusCode(401).contentType(ContentType.JSON)
        .body("error", equalTo("Unauthorized"));
  }

  @Test
  public void putComponentAsRegisteredUserNoAdmin() {
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
        .body("""
            {
              "name": "sampleNamePUT",
              "description": "samepleDescriptionPUT",
              "type" : "CPU",
              "brand" : "sampleBrandPUT",
              "price" : 100.00,
              "stock" : 5
            }
            """)
        .when().put("components/5")
        .then().statusCode(403).contentType(ContentType.JSON)
        .body("error", equalTo("Forbidden"));
  }

  @Test
  public void putComponentAsAdmin() {
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
        .cookie("AuthToken", authToken)
        .header("Content-Type", "application/json")
        .body("""
            {
              "name": "sampleNamePUT",
              "description": "sampleDescriptionPUT",
              "type" : "CPU",
              "brand" : "MSI",
              "price" : 100.00,
              "stock" : 5
            }
            """)
        .when().put("components/5")
        .then().statusCode(201).contentType(ContentType.JSON)
        .body("id", notNullValue())
        .body("name", equalTo("sampleNamePUT"))
        .body("description", equalTo("sampleDescriptionPUT"))
        .body("type", equalTo("CPU"))
        .body("brand", equalTo("MSI"))
        .body("price", equalTo(100.00F))
        .body("stock", equalTo(5));
  }

  @Test
  public void putVoidComponent() {
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
        .cookie("AuthToken", authToken)
        .header("Content-Type", "application/json")
        .body("""
            {
              "name": "",
              "description": "",
              "type": "CPU",
              "brand" : "",
              "price": "",
              "stock": ""
            }
            """)
        .when().put("components/5")
        .then().statusCode(500).contentType(ContentType.JSON)
        .body("message", equalTo(
            "Validación fallida:\n• El nombre del componente es obligatorio.\n• La descripción es obligatoria.\n• La marca es obligatoria.\n• El precio es obligatorio.\n• El stock es obligatorio."));
  }

  @Test
  public void putNullComponent() {
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
        .cookie("AuthToken", authToken)
        .header("Content-Type", "application/json")
        .body("""
            {
              "name": null,
              "description": null,
              "type": "CPU",
              "brand" : null,
              "price": null,
              "stock": null
            }
            """)
        .when().put("components/5")
        .then().statusCode(500).contentType(ContentType.JSON)
        .body("message", equalTo(
            "Validación fallida:\n• El nombre del componente es obligatorio.\n• La descripción es obligatoria.\n• La marca es obligatoria.\n• El precio es obligatorio.\n• El stock es obligatorio."));
  }

  @Test
  public void putExistingComponent() {
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
        .cookie("AuthToken", authToken)
        .header("Content-Type", "application/json")
        .body("""
            {
              "name": "Kingston NV3",
              "description": "samepleDescriptionPUT",
              "type" : "CPU",
              "brand" : "sampleBrandPUT",
              "price" : 100.00,
              "stock" : 5
            }
            """)
        .when().put("components/5")
        .then().statusCode(500).contentType(ContentType.JSON)
        .body("status", equalTo(500))
        .body("message", equalTo("Validación fallida:\n• Ya existe un componente con ese nombre."));
  }

  @Test
  public void putBadFormatComponent() {
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
        .cookie("AuthToken", authToken)
        .header("Content-Type", "application/json")
        .body("""
            {
              "name": "sampleNamePUT",
              "description": "samepleDescriptionPUT",
              "type" : "CPU",
              "brand" : "sampleBrandPUT",
              "price" : -100.00,
              "stock" : -5
            }
            """)
        .when().put("components/5")
        .then().statusCode(500).contentType(ContentType.JSON)
        .body("status", equalTo(500))
        .body("message", equalTo(
            "Validación fallida:\n• El precio del producto ha de ser mayor de 0.00 €.\n• El stock del producto ha de ser 0 o mayor."));
  }

  @Test
  public void putComponentImageAsNoLoggedIn() throws IOException {
    byte[] updatedImage;
    try (InputStream imageResource = getClass().getResourceAsStream("/sample_images/rtx-5060.jpg")) {
      updatedImage = imageResource.readAllBytes();
    }

    given()
        .multiPart("imageFile", "rtx-5060.jpg", updatedImage, "image/jpeg")
        .when().put("components/5/image")
        .then().statusCode(401).contentType(ContentType.JSON)
        .body("error", equalTo("Unauthorized"));
  }

  @Test
  public void putComponentImageAsRegisteredUserNoAdmin() throws IOException {
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

    byte[] updatedImage;
    try (InputStream imageResource = getClass().getResourceAsStream("/sample_images/rtx-5060.jpg")) {
      updatedImage = imageResource.readAllBytes();
    }

    given()
        .cookie("AuthToken", authToken)
        .multiPart("imageFile", "rtx-5060.jpg", updatedImage, "image/jpeg")
        .when().put("components/5/image")
        .then().statusCode(403).contentType(ContentType.JSON)
        .body("error", equalTo("Forbidden"));
  }

  @Test
  public void putComponentImageAsAdmin() throws IOException {
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

    byte[] updatedImage;
    try (InputStream imageResource = getClass().getResourceAsStream("/sample_images/rtx-5060.jpg")) {
      updatedImage = imageResource.readAllBytes();
    }

    given()
        .cookie("AuthToken", authToken)
        .multiPart("imageFile", "rtx-5060.jpg", updatedImage, "image/jpeg")
        .when().put("components/5/image")
        .then().statusCode(201).contentType(ContentType.JSON);

  }

  @Test
  public void loadComponentTypes() {
    given()
        .header("Content-Type", "application/json")
        .when().get("/component-types")
        .then().statusCode(200).contentType(ContentType.JSON)
        .body("", hasSize(7))
        .body("", hasItems("CPU", "GPU", "RAM", "STORAGE", "MOTHERBOARD", "PSU", "CASE"));
  }

}
