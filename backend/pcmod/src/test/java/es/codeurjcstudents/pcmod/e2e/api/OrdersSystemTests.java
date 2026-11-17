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
public class OrdersSystemTests {

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
  public void getOrdersAsNoLoggedIn() {
    given().header("Content-Type", "application/json")
      .when().get("orders/")
      .then().statusCode(401).contentType(ContentType.JSON)
      .body("error", equalTo("Unauthorized"));
  }

  @Test
  public void getOrdersAsLoggedIn() {

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
      .when().get("orders/")
      .then().statusCode(200).contentType(ContentType.JSON)
      // Global checks
      .body("content", hasSize(1))
      // Specific checks
      .body("content[0].status", equalTo("FINISHED"))
      .body("content[0].totalPrice", equalTo(121.99F))
      .body("content[0].shippingAddress", equalTo("c/example_address 1"))
      .body("content[0].userUsername", equalTo("user_example"))
      .body("content[0].items", hasSize(1))
      .body("content[0].items[0].componentName", equalTo("Seagate BarraCuda 3.5"))
      .body("content[0].items[0].quantity", equalTo(1))
      .body("content[0].items[0].unitPriceAtPurchase", equalTo(121.99F));
  }
  
  @Test
  public void getOrderAsNoLoggedIn() {
    given().header("Content-Type", "application/json")
      .when().get("orders/1")
      .then().statusCode(401).contentType(ContentType.JSON)
      .body("error", equalTo("Unauthorized"));
  }

  @Test
  public void getOwnOrderAsLoggedIn() {
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
      .when().get("orders/1")
      .then().statusCode(200).contentType(ContentType.JSON)
      .body("status", equalTo("FINISHED"))
      .body("totalPrice", equalTo(121.99F))
      .body("shippingAddress", equalTo("c/example_address 1"))
      .body("userUsername", equalTo("user_example"))
      .body("items", hasSize(1))
      .body("items[0].componentName", equalTo("Seagate BarraCuda 3.5"))
      .body("items[0].quantity", equalTo(1))
      .body("items[0].unitPriceAtPurchase", equalTo(121.99F));
  }

  @Test
  public void getOtherUserOrderAsLoggedIn() {
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
      .when().get("orders/2")
      .then().statusCode(403).contentType(ContentType.JSON)
      .body("error", equalTo("Forbidden"));
  }

  @Test
  public void getOrderAsAdmin() {
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
      .when().get("orders/1")
      .then().statusCode(200).contentType(ContentType.JSON);

    given()
      .cookie("AuthToken", authToken)  
      .header("Content-Type", "application/json")
      .when().get("orders/2")
      .then().statusCode(200).contentType(ContentType.JSON);
  }

}
