package es.codeurjcstudents.pcmod.e2e.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Tag("server-system")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReviewsSystemTests {

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
  public void getReviewsByComponentId() {
    given().header("Content-Type", "application/json")
        .when().get("components/2/reviews/")
        .then().statusCode(200).contentType(ContentType.JSON)
        // Global checks
        .body("totalElements", equalTo(2))
        // Specific checks (e.g., First Review)
        .body("content[0].id", notNullValue())
        .body("content[0].title", equalTo("Buena gráfica, aunque con cosas a mejorar."))
        .body("content[0].rating", equalTo(4))
        .body("content[0].componentId", equalTo(2))
        .body("content[0].user.id", equalTo(1))
        .body("content[0].user.username", equalTo("user_example"));
  }

  @Test
  public void getZeroReviewsByComponentId() {
    given().header("Content-Type", "application/json")
        .when().get("components/1/reviews/")
        .then().statusCode(200).contentType(ContentType.JSON)
        // Global checks
        .body("content", empty())
        .body("totalElements", equalTo(0))
        .body("totalPages", equalTo(0));
  }

  @Test
  public void postReviewAsNoLoggedIn() {
    given()
        .header("Content-Type", "application/json")
        .body("""
            {
              "title": "sample_title",
              "message": "sample_message",
              "rating": 5
            }
            """)
        .when().post("/components/5/reviews/")
        .then().statusCode(401).contentType(ContentType.JSON)
        .body("error", equalTo("Unauthorized"));
  }

  @Test
  public void postReviewAsRegisteredUserNoAdmin() {
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
              "title": "sample_title",
              "message": "sample_message",
              "rating": 5
            }
            """)
        .when().post("/components/5/reviews/")
        .then().statusCode(201).contentType(ContentType.JSON)
        .body("id", notNullValue())
        .body("title", equalTo("sample_title"))
        .body("message", equalTo("sample_message"))
        .body("rating", equalTo(5));
  }

  @Test
  public void postReviewAsAdmin() {
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
              "title": "sample_title",
              "message": "sample_message",
              "rating": 5
            }
            """)
        .when().post("/components/5/reviews/")
        .then().statusCode(201).contentType(ContentType.JSON)
        .body("id", notNullValue())
        .body("title", equalTo("sample_title"))
        .body("message", equalTo("sample_message"))
        .body("rating", equalTo(5));
  }

  @Test
  public void postReviewForAnExistingOwnReviewComponent() {
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
              "title": "sample_title",
              "message": "sample_message",
              "rating": 5
            }
            """)
        .when().post("/components/4/reviews/")
        .then().statusCode(500).contentType(ContentType.JSON)
        .body("message", equalTo("Validación fallida:\n• Ya existe una reseña de este producto para tu usuario."));
  }

  @Test
  public void postVoidReviewData() {
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
              "title": "",
              "message": "",
              "rating": ""
            }
            """)
        .when().post("/components/5/reviews/")
        .then().statusCode(500).contentType(ContentType.JSON)
        .body("message", equalTo(
            "Validación fallida:\n• El título es obligatorio.\n• El mensaje es obligatorio.\n• La valoración es obligatoria."));
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
              "title": null,
              "message": null,
              "rating": null
            }
            """)
        .when().post("/components/5/reviews/")
        .then().statusCode(500).contentType(ContentType.JSON)
        .body("message", equalTo(
            "Validación fallida:\n• El título es obligatorio.\n• El mensaje es obligatorio.\n• La valoración es obligatoria."));
  }

  @Test
  public void postBadNumReviewData() {
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
              "title": "sample_title",
              "message": "sample_message",
              "rating": -5
            }
            """)
        .when().post("/components/5/reviews/")
        .then().statusCode(500).contentType(ContentType.JSON)
        .body("status", equalTo(500))
        .body("message", equalTo(
            "Validación fallida:\n• La valoración del producto ha de estar entre 1 y 5."));
  }

  @Test
  public void hasReviewed() {
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
        .when().get("/components/2/reviews/has-reviewed")
        .then().statusCode(200).contentType(ContentType.JSON)
        .body(equalTo("true"));
  }

  @Test
  public void hasNotReviewed() {
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
        .when().get("/components/3/reviews/has-reviewed")
        .then().statusCode(200).contentType(ContentType.JSON)
        .body(equalTo("false"));
  } 

  @Test
  public void hasReviewedWithNullUser() {
    given()
        .when().get("/components/2/reviews/has-reviewed")
        .then().statusCode(200).contentType(ContentType.JSON)
        .body(equalTo("false"));
  } 

  @Test
  public void deleteReviewAsNotLoggedIn() {

    given()
        .when().delete("components/2/reviews/1")
        .then().statusCode(401).contentType(ContentType.JSON)
        .body("status", equalTo(401))
        .body("error", equalTo("Unauthorized"));
  }

  @Test
  public void deleteOwnReview() {

    given().header("Content-Type", "application/json")
        .when().get("components/2/reviews/")
        .then().statusCode(200).contentType(ContentType.JSON)
        .body("totalElements", equalTo(2));

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
        .when().delete("components/2/reviews/1")
        .then().statusCode(200);

    given().header("Content-Type", "application/json")
        .when().get("components/2/reviews/")
        .then().statusCode(200).contentType(ContentType.JSON)
        .body("totalElements", equalTo(1));

  }

  @Test
  public void deleteOtherReviewAsNoAdmin() {
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
        .when().delete("components/2/reviews/2")
        .then().statusCode(403).contentType(ContentType.JSON)
        .body("error", equalTo("Forbidden"));
  }

  @Test
  public void deleteReviewsAsAdmin() {

    given().header("Content-Type", "application/json")
        .when().get("components/2/reviews/")
        .then().statusCode(200).contentType(ContentType.JSON)
        .body("totalElements", equalTo(2));

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
        .when().delete("components/2/reviews/1")
        .then().statusCode(200);

    given()
        .header("Content-Type", "application/json")
        .cookie("AuthToken", authToken)
        .when().delete("components/2/reviews/2")
        .then().statusCode(200);

    given().header("Content-Type", "application/json")
        .when().get("components/2/reviews/")
        .then().statusCode(200).contentType(ContentType.JSON)
        .body("totalElements", equalTo(0));

  }

  @Test
  public void deleteReviewWithWrongComponentId() {

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
        .when().delete("components/2/reviews/4")
        .then().statusCode(500).contentType(ContentType.JSON)
        .body("message", equalTo("La reseña no pertenece a ese componente."));

  }

  @Test
  public void putReviewAsNoLoggedIn() {
    given()
        .header("Content-Type", "application/json")
        .body("""
            {
              "title": "sample_title",
              "message": "sample_message",
              "rating": 5
            }
            """)
        .when().put("/components/2/reviews/1")
        .then().statusCode(401).contentType(ContentType.JSON)
        .body("error", equalTo("Unauthorized"));
  }

  @Test
  public void putReviewAsRegisteredUserNoAdmin() {
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
              "title": "sample_title",
              "message": "sample_message",
              "rating": 5
            }
            """)
        .when().put("/components/2/reviews/1")
        .then().statusCode(201).contentType(ContentType.JSON)
        .body("id", notNullValue())
        .body("title", equalTo("sample_title"))
        .body("message", equalTo("sample_message"))
        .body("rating", equalTo(5));
  }

  @Test
  public void putOtherUserReviewAsRegisteredUserNoAdmin() {
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
              "title": "sample_title",
              "message": "sample_message",
              "rating": 5
            }
            """)
        .when().put("/components/2/reviews/2")
        .then().statusCode(403).contentType(ContentType.JSON)
        .body("error", equalTo("Forbidden"));
  }

  @Test
  public void putReviewsAsAdmin() {
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
              "title": "sample_title",
              "message": "sample_message",
              "rating": 5
            }
            """)
        .when().put("/components/2/reviews/1")
        .then().statusCode(201).contentType(ContentType.JSON)
        .body("id", notNullValue())
        .body("title", equalTo("sample_title"))
        .body("message", equalTo("sample_message"))
        .body("rating", equalTo(5));

    given()
        .cookie("AuthToken", authToken)
        .header("Content-Type", "application/json")
        .body("""
            {
              "title": "sample_title",
              "message": "sample_message",
              "rating": 5
            }
            """)
        .when().put("/components/2/reviews/2")
        .then().statusCode(201).contentType(ContentType.JSON)
        .body("id", notNullValue())
        .body("title", equalTo("sample_title"))
        .body("message", equalTo("sample_message"))
        .body("rating", equalTo(5));
  }

  @Test
  public void putVoidReviewData() {
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
              "title": "",
              "message": "",
              "rating": ""
            }
            """)
        .when().put("/components/2/reviews/1")
        .then().statusCode(500).contentType(ContentType.JSON)
        .body("message", equalTo(
            "Validación fallida:\n• El título es obligatorio.\n• El mensaje es obligatorio.\n• La valoración es obligatoria."));
  }

  @Test
  public void putNullComponentData() {
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
              "title": null,
              "message": null,
              "rating": null
            }
            """)
        .when().put("/components/2/reviews/1")
        .then().statusCode(500).contentType(ContentType.JSON)
        .body("message", equalTo(
            "Validación fallida:\n• El título es obligatorio.\n• El mensaje es obligatorio.\n• La valoración es obligatoria."));
  }

  @Test
  public void putBadNumReviewData() {
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
              "title": "sample_title",
              "message": "sample_message",
              "rating": -5
            }
            """)
        .when().put("/components/2/reviews/1")
        .then().statusCode(500).contentType(ContentType.JSON)
        .body("status", equalTo(500))
        .body("message", equalTo(
            "Validación fallida:\n• La valoración del producto ha de estar entre 1 y 5."));
  }

}
