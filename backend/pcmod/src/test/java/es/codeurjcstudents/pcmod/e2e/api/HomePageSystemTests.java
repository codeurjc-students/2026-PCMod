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
public class HomePageSystemTests {

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
        .when().get("/components/recent")
        .then().statusCode(200).contentType(ContentType.JSON)
        // Global checks
        .body("size()", equalTo(3))
        // Specific checks
        .body("[0].name", equalTo("Kingston FURY Beast"))
        .body("[1].name", equalTo("Seagate BarraCuda 3.5"))
        .body("[2].name", equalTo("AMD Radeon RX 9060 XT DUAL WHITE"));
  }

}
