package es.codeurjcstudents.pcmod.e2e.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Tag("client-system")
public class ReviewsSystemTests {

  private WebDriver driver;
  private WebDriverWait wait;

  @BeforeEach
  public void setupTest() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--incognito");
    options.addArguments("--disable-notifications");
    options.addArguments("--disable-features=PasswordLeakDetection");
    options.addArguments("--headless");
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    options.addArguments("--window-size=1920,1080");

    driver = new ChromeDriver(options);
    wait = new WebDriverWait(driver, Duration.ofSeconds(10));
  }

  @AfterEach
  public void teardown() {
    if (driver != null) {
      driver.quit();
    }
  }

  private void scrollToAndClick(By locator) {

    WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));

    new Actions(driver)
        .scrollToElement(element)
        .perform();

    wait.until(ExpectedConditions.elementToBeClickable(element)).click();

  }

  private void login(String email, String pass) {

    driver.get("http://localhost:5173/login");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
    driver.findElement(By.id("email")).sendKeys(email);
    driver.findElement(By.id("password")).sendKeys(pass);

    scrollToAndClick(By.name("login-button"));

    wait.until(ExpectedConditions.urlToBe("http://localhost:5173/"));

  }

  @Test
  public void componentDetailWithoutReviewsRenderTest() {

    driver.get("http://localhost:5173/components/1");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-reviews-title")));
    String loadedComponentReviewsTitle = driver.findElement(By.id("component-reviews-title")).getText();
    assertThat(loadedComponentReviewsTitle).isEqualTo("Reseñas del producto:");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-average-rating")));
    String loadedComponentAverageRating = driver.findElement(By.id("component-average-rating")).getText();
    assertThat(loadedComponentAverageRating).isEqualTo("Puntuación media: ☆☆☆☆☆ 0/5 (0 reseña(s))");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-no-reviews")));
    String loadedComponentNoReviews = driver.findElement(By.id("component-no-reviews")).getText();
    assertThat(loadedComponentNoReviews).isEqualTo("No hay reseñas de este producto todavía.");

  }

  @Test
  public void componentDetailWithReviewsRenderTest() {

    driver.get("http://localhost:5173/components/2");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-reviews-title")));
    String loadedComponentReviewsTitle = driver.findElement(By.id("component-reviews-title")).getText();
    assertThat(loadedComponentReviewsTitle).isEqualTo("Reseñas del producto:");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-average-rating")));
    String loadedComponentAverageRating = driver.findElement(By.id("component-average-rating")).getText();
    assertThat(loadedComponentAverageRating).isEqualTo("Puntuación media: ★★★★☆ 3.5/5 (2 reseña(s))");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("usernameReview-1")));
    String loadedUserExample = driver.findElement(By.id("usernameReview-1")).getText();
    assertThat(loadedUserExample).isEqualTo("user_example");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("review-title-1")));
    String loadedUserReview = driver.findElement(By.id("review-title-1")).getText();
    assertThat(loadedUserReview).isEqualTo("Buena gráfica, aunque con cosas a mejorar.");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("review-valoration-1")));
    String loadedUserRating = driver.findElement(By.id("review-valoration-1")).getText();
    assertThat(loadedUserRating).isEqualTo("Valoración del producto: ★★★★☆ 4/5");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("usernameReview-2")));
    String loadedAdminExample = driver.findElement(By.id("usernameReview-2")).getText();
    assertThat(loadedAdminExample).isEqualTo("admin_example");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("review-title-2")));
    String loadedAdminReview = driver.findElement(By.id("review-title-2")).getText();
    assertThat(loadedAdminReview).isEqualTo("Tarjeta gráfica bastante mejorable.");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("review-valoration-2")));
    String loadedAdminRating = driver.findElement(By.id("review-valoration-2")).getText();
    assertThat(loadedAdminRating).isEqualTo("Valoración del producto: ★★★☆☆ 3/5");

    assertThat(driver.findElements(By.id("loadMore"))).isEmpty();

  }
  
  @Test
  public void reviewsOptionsAsNonLoggedInTest() {

    driver.get("http://localhost:5173/components/2");

    assertThat(driver.findElements(By.id("save-review-button"))).isEmpty();
    assertThat(driver.findElements(By.id("update-review-button-1"))).isEmpty();
    assertThat(driver.findElements(By.id("update-review-button-2"))).isEmpty();
    assertThat(driver.findElements(By.id("delete-review-button-1"))).isEmpty();
    assertThat(driver.findElements(By.id("delete-review-button-2"))).isEmpty();

  }

  @Test
  public void reviewsOptionsAsLoggedInNoAdminWithReviewTest() {

    login("user@example.com", "userpass");

    wait.until(ExpectedConditions.urlToBe("http://localhost:5173/"));

    driver.get("http://localhost:5173/components/2");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("update-review-button-1")));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("delete-review-button-1")));

    assertThat(driver.findElements(By.id("save-review-button"))).isEmpty();
    assertThat(driver.findElements(By.id("update-review-button-1"))).isNotEmpty();
    assertThat(driver.findElements(By.id("update-review-button-2"))).isEmpty();
    assertThat(driver.findElements(By.id("delete-review-button-1"))).isNotEmpty();
    assertThat(driver.findElements(By.id("delete-review-button-2"))).isEmpty();

  }

  @Test
  public void reviewsOptionsAsLoggedInWithoutReviewTest() {

    login("user@example.com", "userpass");

    wait.until(ExpectedConditions.urlToBe("http://localhost:5173/"));

    driver.get("http://localhost:5173/components/1");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("save-review-button")));

    assertThat(driver.findElements(By.id("save-review-button"))).isNotEmpty();

  }

  @Test
  public void reviewsOptionsAsAdminWithReviewTest() {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components/2");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("update-review-button-1")));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("update-review-button-2")));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("delete-review-button-1")));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("delete-review-button-2")));

    assertThat(driver.findElements(By.id("save-review-button"))).isEmpty();
    assertThat(driver.findElements(By.id("update-review-button-1"))).isNotEmpty();
    assertThat(driver.findElements(By.id("update-review-button-2"))).isNotEmpty();
    assertThat(driver.findElements(By.id("delete-review-button-1"))).isNotEmpty();
    assertThat(driver.findElements(By.id("delete-review-button-2"))).isNotEmpty();

  }

  @Test
  public void reviewCreateRenderTest() {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components/1");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("review-heading")));
    String reviewHeading = driver.findElement(By.id("review-heading")).getText();
    assertThat(reviewHeading).isEqualTo("¡Añade una reseña a este producto!");

    assertThat(driver.findElements(By.id("error-message"))).isEmpty();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("review-title-tag")));
    assertThat(driver.findElement(By.id("review-title-tag")).getText()).isEqualTo("Título:");
    assertThat(driver.findElement(By.id("invalid-title")).isDisplayed()).isFalse();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("message-title")));
    assertThat(driver.findElement(By.id("message-title")).getText()).isEqualTo("Mensaje:");
    assertThat(driver.findElement(By.id("invalid-message")).isDisplayed()).isFalse();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("rating-title")));
    assertThat(driver.findElement(By.id("rating-title")).getText()).isEqualTo("Valoración:");
    assertThat(driver.findElement(By.id("invalid-rating")).isDisplayed()).isFalse();

    assertThat(driver.findElements(By.name("save-review-button"))).isNotEmpty();

  }

  @Test
  public void createReviewTest() {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components/5");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("title")));
    driver.findElement(By.id("title")).sendKeys("sample_title");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("message")));
    driver.findElement(By.id("message")).sendKeys("sample_message");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("rating")));
    driver.findElement(By.id("rating")).sendKeys("5");

    scrollToAndClick(By.name("save-review-button"));

    driver.get("http://localhost:5173/components/5");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-average-rating")));
    String loadedComponentAverageRating = driver.findElement(By.id("component-average-rating")).getText();
    assertThat(loadedComponentAverageRating).isEqualTo("Puntuación media: ★★★★★ 5/5 (1 reseña(s))");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("usernameReview-2")));
    String savedUserReview = driver.findElement(By.id("usernameReview-2")).getText();
    assertThat(savedUserReview).isEqualTo("admin_example");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("review-title-5")));
    String reviewTitle = driver.findElement(By.id("review-title-5")).getText();
    assertThat(reviewTitle).isEqualTo("sample_title");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("review-valoration-5")));
    String reviewRating = driver.findElement(By.id("review-valoration-5")).getText();
    assertThat(reviewRating).isEqualTo("Valoración del producto: ★★★★★ 5/5");

  }

  @Test
  public void createVoidReviewTest() {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components/1");

    scrollToAndClick(By.name("save-review-button"));

    assertThat(driver.findElement(By.id("invalid-title")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-title")).getText())
        .isEqualTo("Por favor, ingrese un título válido.");
    assertThat(driver.findElement(By.id("invalid-message")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-message")).getText())
        .isEqualTo("Por favor, ingrese un mensaje válido.");
    assertThat(driver.findElement(By.id("invalid-rating")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-rating")).getText())
        .isEqualTo("Por favor, ingrese una valoración válida.");

  }

  @Test
  public void createInvalidReviewTest() {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components/1");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("title")));
    driver.findElement(By.id("title")).sendKeys("sample_title");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("message")));
    driver.findElement(By.id("message")).sendKeys("sample_message");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("rating")));
    driver.findElement(By.id("rating")).sendKeys("-5");

    scrollToAndClick(By.name("save-review-button"));
    
    assertThat(driver.findElement(By.id("invalid-rating")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-rating")).getText())
        .isEqualTo("Por favor, ingrese una valoración válida.");

  }

  @Test
  public void deleteReviewTest() {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components/3");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("delete-review-button-3")));
    scrollToAndClick(By.id("delete-review-button-3"));
    WebElement deleteModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal.show")));
    assertThat(deleteModal.findElement(By.className("modal-title")).getText())
        .isEqualTo("¿Está seguro de que desea borrar esta reseña?");
    assertThat(deleteModal.findElement(By.className("modal-body")).getText())
        .contains("Toda su información se perderá y no se podrá recuperar.")
        .contains("Esta acción no se puede deshacer.");
    scrollToAndClick(By.cssSelector(".modal-footer .pcmod-danger-button"));

    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("review-title-3")));
  }

  @Test
  public void cancelDeleteReviewTest() {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components/2");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("delete-review-button-2")));
    scrollToAndClick(By.id("delete-review-button-2"));
    WebElement deleteModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal.show")));
    assertThat(deleteModal.findElement(By.className("modal-title")).getText())
        .isEqualTo("¿Está seguro de que desea borrar esta reseña?");
    assertThat(deleteModal.findElement(By.className("modal-body")).getText())
        .contains("Toda su información se perderá y no se podrá recuperar.")
        .contains("Esta acción no se puede deshacer.");
    scrollToAndClick(By.cssSelector(".modal-footer .btn-secondary"));

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("review-title-2")));
    assertThat(driver.findElement(By.id("review-title-2")).getText())
      .isEqualTo("Tarjeta gráfica bastante mejorable.");

  }

  @Test
  public void cancelDeleteComponentClosingModalTest() {

    login("admin@example.com", "adminpass");
    
    driver.get("http://localhost:5173/components/2");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("delete-review-button-2")));
    scrollToAndClick(By.id("delete-review-button-2"));
    WebElement deleteModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal.show")));
    assertThat(deleteModal.findElement(By.className("modal-title")).getText())
        .isEqualTo("¿Está seguro de que desea borrar esta reseña?");
    assertThat(deleteModal.findElement(By.className("modal-body")).getText())
        .contains("Toda su información se perderá y no se podrá recuperar.")
        .contains("Esta acción no se puede deshacer.");
    scrollToAndClick(By.cssSelector(".modal-header .btn-close"));

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("review-title-2")));
    assertThat(driver.findElement(By.id("review-title-2")).getText())
        .isEqualTo("Tarjeta gráfica bastante mejorable.");

  }

  @Test
  public void updateAsLoggedInNoAdminTest() {

    login("user@example.com", "userpass");

    driver.get("http://localhost:5173/components/4");
    scrollToAndClick(By.id("update-review-button-4"));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("title")));
    driver.findElement(By.id("title")).clear();
    driver.findElement(By.id("title")).sendKeys("updated_title");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("message")));
    driver.findElement(By.id("message")).clear();
    driver.findElement(By.id("message")).sendKeys("updated_message");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("rating")));
    driver.findElement(By.id("rating")).clear();
    driver.findElement(By.id("rating")).sendKeys("1");
    scrollToAndClick(By.id("save-review-button"));

    driver.get("http://localhost:5173/components/4");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("usernameReview-1")));
    String reviewUsername = driver.findElement(By.id("usernameReview-1")).getText();
    assertThat(reviewUsername).isEqualTo("user_example");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("review-title-4")));
    String reviewTitle = driver.findElement(By.id("review-title-4")).getText();
    assertThat(reviewTitle).isEqualTo("updated_title");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("review-message-4")));
    String reviewMessage = driver.findElement(By.id("review-message-4")).getText();
    assertThat(reviewMessage).isEqualTo("updated_message");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("review-valoration-4")));
    String reviewRating = driver.findElement(By.id("review-valoration-4")).getText();
    assertThat(reviewRating).isEqualTo("Valoración del producto: ★☆☆☆☆ 1/5");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-average-rating")));
    String loadedComponentAverageRating = driver.findElement(By.id("component-average-rating")).getText();
    assertThat(loadedComponentAverageRating).isEqualTo("Puntuación media: ★☆☆☆☆ 1/5 (1 reseña(s))");

  }

  @Test
  public void updateAsAdminTest() {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components/4");
    scrollToAndClick(By.id("update-review-button-4"));
    WebElement editForm = wait.until(
      ExpectedConditions.visibilityOfElementLocated(By.id("edit-review-form"))
    );
    editForm.findElement(By.id("title")).clear();
    editForm.findElement(By.id("title")).sendKeys("updated_title");
    editForm.findElement(By.id("message")).clear();
    editForm.findElement(By.id("message")).sendKeys("updated_message");
    editForm.findElement(By.id("rating")).clear();
    editForm.findElement(By.id("rating")).sendKeys("1");
    scrollToAndClick(By.id("save-review-button"));

    driver.get("http://localhost:5173/components/4");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("usernameReview-1")));
    String reviewUsername = driver.findElement(By.id("usernameReview-1")).getText();
    assertThat(reviewUsername).isEqualTo("user_example");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("review-title-4")));
    String reviewTitle = driver.findElement(By.id("review-title-4")).getText();
    assertThat(reviewTitle).isEqualTo("updated_title");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("review-message-4")));
    String reviewMessage = driver.findElement(By.id("review-message-4")).getText();
    assertThat(reviewMessage).isEqualTo("updated_message");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("review-valoration-4")));
    String reviewRating = driver.findElement(By.id("review-valoration-4")).getText();
    assertThat(reviewRating).isEqualTo("Valoración del producto: ★☆☆☆☆ 1/5");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-average-rating")));
    String loadedComponentAverageRating = driver.findElement(By.id("component-average-rating")).getText();
    assertThat(loadedComponentAverageRating).isEqualTo("Puntuación media: ★☆☆☆☆ 1/5 (1 reseña(s))");

  }

  @Test
  public void updateVoidReviewTest() {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components/4");

    scrollToAndClick(By.id("update-review-button-4"));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("title")));
    driver.findElement(By.id("title")).clear();
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("message")));
    driver.findElement(By.id("message")).clear();
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("rating")));
    driver.findElement(By.id("rating")).clear();
    scrollToAndClick(By.id("save-review-button"));

    assertThat(driver.findElement(By.id("invalid-title")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-title")).getText())
        .isEqualTo("Por favor, ingrese un título válido.");
    assertThat(driver.findElement(By.id("invalid-message")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-message")).getText())
        .isEqualTo("Por favor, ingrese un mensaje válido.");
    assertThat(driver.findElement(By.id("invalid-rating")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-rating")).getText())
        .isEqualTo("Por favor, ingrese una valoración válida.");

  }

  @Test
  public void updateInvalidFormatReviewTest() {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components/4");
    scrollToAndClick(By.id("update-review-button-4"));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("title")));
    driver.findElement(By.id("title")).clear();
    driver.findElement(By.id("title")).sendKeys("updated_title");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("message")));
    driver.findElement(By.id("message")).clear();
    driver.findElement(By.id("message")).sendKeys("updated_message");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("rating")));
    driver.findElement(By.id("rating")).clear();
    driver.findElement(By.id("rating")).sendKeys("-1");
    scrollToAndClick(By.id("save-review-button"));

    assertThat(driver.findElement(By.id("invalid-rating")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-rating")).getText())
        .isEqualTo("Por favor, ingrese una valoración válida.");

  }
  
  @Test
  public void cancelUpdateReviewTest() {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components/4");
    scrollToAndClick(By.id("update-review-button-4"));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("title")));
    driver.findElement(By.id("title")).clear();
    driver.findElement(By.id("title")).sendKeys("updated_title_cancel");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("message")));
    driver.findElement(By.id("message")).clear();
    driver.findElement(By.id("message")).sendKeys("updated_message_cancel");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("rating")));
    driver.findElement(By.id("rating")).clear();
    driver.findElement(By.id("rating")).sendKeys("2");
    scrollToAndClick(By.name("cancel-review-button"));

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("usernameReview-1")));
    String reviewUsername = driver.findElement(By.id("usernameReview-1")).getText();
    assertThat(reviewUsername).isEqualTo("user_example");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("review-title-4")));
    String reviewTitle = driver.findElement(By.id("review-title-4")).getText();
    assertThat(reviewTitle).isNotEqualTo("updated_title_cancel");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("review-message-4")));
    String reviewMessage = driver.findElement(By.id("review-message-4")).getText();
    assertThat(reviewMessage).isNotEqualTo("updated_message_cancel");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("review-valoration-4")));
    String reviewRating = driver.findElement(By.id("review-valoration-4")).getText();
    assertThat(reviewRating).isNotEqualTo("2");


  }

}
