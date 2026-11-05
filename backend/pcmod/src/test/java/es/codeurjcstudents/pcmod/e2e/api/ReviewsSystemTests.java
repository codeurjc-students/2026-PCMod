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
public class ReviewsSystemTests {
    
  @LocalServerPort
  private int port;

  @BeforeEach
  public void setup() {
    RestAssured.port = port;
    RestAssured.baseURI = "https://localhost";
    RestAssured.basePath = "/api/v1/components";
    RestAssured.useRelaxedHTTPSValidation();
  }

  @Test 
  public void getReviewsByComponentId(){
      given().header("Content-Type", "application/json")
        .when().get("/2/reviews/")
        .then().statusCode(200).contentType(ContentType.JSON)
        // Global checks
        .body("totalElements", equalTo(2))
        // Specific checks (e.g., First Review)
        .body("content[0].id", notNullValue())
        .body("content[0].title", equalTo("Buena gráfica, aunque con cosas a mejorar."))
        .body("content[0].rating", equalTo(4))
        .body("content[0].componentId", equalTo(2))
        .body("content[0].userId", equalTo(1));
  }

  @Test 
  public void getZeroReviewsByComponentId(){
      given().header("Content-Type", "application/json")
        .when().get("/1/reviews/")
        .then().statusCode(200).contentType(ContentType.JSON)
        // Global checks
        .body("content", empty())
        .body("totalElements", equalTo(0))
        .body("totalPages", equalTo(0));
  }
}
