package es.codeurjcstudents.pcmod.e2e;

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
        .body("totalElements", equalTo(11))
        .body("content", hasSize(11))
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

}
