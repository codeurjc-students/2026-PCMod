package es.codeurjcstudents.pcmod.e2e.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

@Tag("client-system")
public class ComponentsSystemTests {

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
  public void loadComponentsTest() {

    driver.get("http://localhost:5173/components");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-1")));
    String componentName = driver.findElement(By.id("component-1")).getText();
    assertThat(componentName).isEqualTo("AMD Ryzen 7 7800X3D");

    assertThat(driver.findElements(By.id("component-11"))).isEmpty();

    scrollToAndClick(By.name("loadMore"));

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-11")));
    String loadedComponentName = driver.findElement(By.id("component-11")).getText();
    assertThat(loadedComponentName).isEqualTo("Kingston FURY Beast");

    assertThat(driver.findElements(By.name("loadMore"))).isEmpty();

  }

  @Test
  public void componentCreationOptionAsNonLoggedInTest() {

    driver.get("http://localhost:5173/components");

    assertThat(driver.findElements(By.id("create-component-button"))).isEmpty();

  }

  @Test
  public void componentCreationOptionAsLoggedInNoAdminTest() {

    login("user@example.com", "userpass");

    driver.get("http://localhost:5173/components");

    assertThat(driver.findElements(By.id("create-component-button"))).isEmpty();

  }

  @Test
  public void componentCreationOptionAsAdminTest() {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("create-component-button")));

    assertThat(driver.findElements(By.id("create-component-button"))).isNotEmpty();

  }

  @Test
  public void loadEmptyComponentsTest() {

    driver.get("http://localhost:5173/components/?page=2");

    WebElement emptyMessageElement = wait.until(
        ExpectedConditions.presenceOfElementLocated(By.id("empty-message")));
    String emptyMessage = emptyMessageElement.getText();

    assertThat(emptyMessage).isEqualTo("No se han encontrado componentes de PC.");
    assertThat(driver.findElements(By.cssSelector("[id^='component-']"))).isEmpty();
    assertThat(driver.findElements(By.name("loadMore"))).isEmpty();

  }

  @Test
  public void loadErrorTest() {

    driver.get("http://localhost:5173/components");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.name("loadMore")));

    // Simulate a network error
    ((ChromeDriver) driver).executeCdpCommand("Network.enable", Map.of());
    ((ChromeDriver) driver).executeCdpCommand("Network.emulateNetworkConditions", Map.of(
        "offline", true,
        "latency", 0,
        "downloadThroughput", 0,
        "uploadThroughput", 0));

    scrollToAndClick(By.name("loadMore"));

    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[role='alert']")));
    String errorMessage = driver.findElement(By.cssSelector("[role='alert']")).getText();
    assertThat(errorMessage).isEqualTo("Hubo un problema al cargar más componentes. Por favor, pruebe de nuevo.");

  }

  @Test
  public void componentDetailRenderTest() {

    driver.get("http://localhost:5173/components/1");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-stock")));
    String componentStockMessage = driver.findElement(By.id("component-stock")).getText();
    assertThat(componentStockMessage).isEqualTo("¡Últimas unidades!");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-name")));
    String loadedComponentName = driver.findElement(By.id("component-name")).getText();
    assertThat(loadedComponentName).isEqualTo("AMD Ryzen 7 7800X3D");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-description")));
    String loadedComponentDescription = driver.findElement(By.id("component-description")).getText();
    assertThat(loadedComponentDescription).contains("8 núcleos y 16 hilos ideales para gaming");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-brand-type")));
    String loadedComponentBrandType = driver.findElement(By.id("component-brand-type")).getText();
    assertThat(loadedComponentBrandType).isEqualTo("AMD | CPU");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-price")));
    String loadedComponentPrice = driver.findElement(By.id("component-price")).getText();
    assertThat(loadedComponentPrice).isEqualTo("350.00 €");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-reviews-title")));
    String loadedComponentReviewsTitle = driver.findElement(By.id("component-reviews-title")).getText();
    assertThat(loadedComponentReviewsTitle).isEqualTo("Reseñas del producto:");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-average-rating")));
    String loadedComponentAverageRating = driver.findElement(By.id("component-average-rating")).getText();
    assertThat(loadedComponentAverageRating).isEqualTo("Puntuación media: N/A");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-no-reviews")));
    String loadedComponentNoReviews = driver.findElement(By.id("component-no-reviews")).getText();
    assertThat(loadedComponentNoReviews).isEqualTo("No hay reseñas de este producto todavía.");

  }

  @Test
  public void componentDetailRenderNoLastUnitsTest() {

    driver.get("http://localhost:5173/components/3");
        
    assertThat(driver.findElements(By.id("component-stock"))).isEmpty();

  }

  @Test
  public void componentDetailRenderLastUnitsTest() {

    driver.get("http://localhost:5173/components/1");
        
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-stock")));
    String componentStockMessage = driver.findElement(By.id("component-stock")).getText();
    assertThat(componentStockMessage).isEqualTo("¡Últimas unidades!");

  }

  @Test
  public void componentDetailOptionsAsNonLoggedInTest() {

    driver.get("http://localhost:5173/components/1");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("add-to-cart-button")));

    assertThat(driver.findElements(By.id("add-to-cart-button"))).isNotEmpty();
    assertThat(driver.findElements(By.id("edit-component-button"))).isEmpty();
    assertThat(driver.findElements(By.id("delete-component-button"))).isEmpty();

  }

  @Test
  public void componentDetailOptionsAsLoggedInNoAdminTest() {

    login("user@example.com", "userpass");

    wait.until(ExpectedConditions.urlToBe("http://localhost:5173/"));

    driver.get("http://localhost:5173/components/1");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("add-to-cart-button")));

    assertThat(driver.findElements(By.id("add-to-cart-button"))).isNotEmpty();
    assertThat(driver.findElements(By.id("edit-component-button"))).isEmpty();
    assertThat(driver.findElements(By.id("delete-component-button"))).isEmpty();

  }

  @Test
  public void componentDetailOptionsAsAdminTest() {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components/1");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("add-to-cart-button")));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("edit-component-button")));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("delete-component-button")));

    assertThat(driver.findElements(By.id("add-to-cart-button"))).isNotEmpty();
    assertThat(driver.findElements(By.id("edit-component-button"))).isNotEmpty();
    assertThat(driver.findElements(By.id("delete-component-button"))).isNotEmpty();

  }

  @Test
  public void componentCreateRenderTest() {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components/create");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("create-component-title")));
    String registerTitle = driver.findElement(By.id("create-component-title")).getText();
    assertThat(registerTitle).isEqualTo("Ingrese los datos del componente:");

    assertThat(driver.findElements(By.id("error-message"))).isEmpty();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name-title")));
    assertThat(driver.findElement(By.id("name-title")).getText()).isEqualTo("Nombre:");
    assertThat(driver.findElements(By.id("name"))).isNotEmpty();
    assertThat(driver.findElement(By.id("invalid-name")).isDisplayed()).isFalse();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("description-title")));
    assertThat(driver.findElement(By.id("description-title")).getText()).isEqualTo("Descripción:");
    assertThat(driver.findElements(By.id("description"))).isNotEmpty();
    assertThat(driver.findElement(By.id("invalid-description")).isDisplayed()).isFalse();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("brand-title")));
    assertThat(driver.findElement(By.id("brand-title")).getText()).isEqualTo("Marca:");
    assertThat(driver.findElements(By.id("brand"))).isNotEmpty();
    assertThat(driver.findElement(By.id("invalid-brand")).isDisplayed()).isFalse();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("type-title")));
    assertThat(driver.findElement(By.id("type-title")).getText()).isEqualTo("Tipo de componente:");
    assertThat(driver.findElements(By.id("type"))).isNotEmpty();
    assertThat(driver.findElement(By.id("invalid-type")).isDisplayed()).isFalse();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("price-title")));
    assertThat(driver.findElement(By.id("price-title")).getText()).isEqualTo("Precio:");
    assertThat(driver.findElements(By.id("price"))).isNotEmpty();
    assertThat(driver.findElement(By.id("invalid-price")).isDisplayed()).isFalse();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("stock-title")));
    assertThat(driver.findElement(By.id("stock-title")).getText()).isEqualTo("Stock:");
    assertThat(driver.findElements(By.id("stock"))).isNotEmpty();
    assertThat(driver.findElement(By.id("invalid-stock")).isDisplayed()).isFalse();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("image-title")));
    assertThat(driver.findElement(By.id("image-title")).getText()).isEqualTo("Imagen del componente:");
    assertThat(driver.findElements(By.id("image"))).isNotEmpty();
    assertThat(driver.findElement(By.id("invalid-image")).isDisplayed()).isFalse();

    assertThat(driver.findElements(By.name("save-component-button"))).isNotEmpty();
    assertThat(driver.findElements(By.id("cancel-component-button"))).isNotEmpty();

  }

  @Test
  public void createComponentTest() throws Exception {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components/create");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name")));
    driver.findElement(By.id("name")).sendKeys("testComponent");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("description")));
    driver.findElement(By.id("description")).sendKeys("testDescription");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("brand")));
    driver.findElement(By.id("brand")).sendKeys("testBrand");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("type")));
    Select select = new Select(driver.findElement(By.id("type")));
    select.selectByValue("CPU");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("price")));
    driver.findElement(By.id("price")).sendKeys("5");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("stock")));
    driver.findElement(By.id("stock")).sendKeys("10");

    String imagePath = Files.createTempFile("valid-image", ".png").toString();
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("image")));
    driver.findElement(By.id("image")).sendKeys(imagePath);

    scrollToAndClick(By.name("save-component-button"));

    wait.until(ExpectedConditions.urlMatches(".*/components/\\d+"));
    
    Files.deleteIfExists(Paths.get(imagePath));

  }

  @Test
  public void createVoidComponentTest() {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components/create");

    scrollToAndClick(By.name("save-component-button"));

    assertThat(driver.findElement(By.id("invalid-name")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-name")).getText())
        .isEqualTo("Por favor, ingrese un nombre válido.");
    assertThat(driver.findElement(By.id("invalid-description")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-description")).getText())
        .isEqualTo("Por favor, ingrese una descripción válida.");
    assertThat(driver.findElement(By.id("invalid-brand")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-brand")).getText())
        .isEqualTo("Por favor, ingrese una marca válida.");
    assertThat(driver.findElement(By.id("invalid-type")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-type")).getText())
        .isEqualTo("Por favor, seleccione un tipo válido.");
    assertThat(driver.findElement(By.id("invalid-price")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-price")).getText())
        .isEqualTo("Por favor, ingrese un precio válido.");
    assertThat(driver.findElement(By.id("invalid-stock")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-stock")).getText())
        .isEqualTo("Por favor, ingrese un stock válido.");
    assertThat(driver.getCurrentUrl()).isEqualTo("http://localhost:5173/components/create");

  }

  @Test
  public void createExistingComponentTest() {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components/create");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name")));
    driver.findElement(By.id("name")).sendKeys("Kingston NV3");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("description")));
    driver.findElement(By.id("description")).sendKeys("testDescription");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("brand")));
    driver.findElement(By.id("brand")).sendKeys("testBrand");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("type")));
    Select select = new Select(driver.findElement(By.id("type")));
    select.selectByValue("CPU");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("price")));
    driver.findElement(By.id("price")).sendKeys("5");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("stock")));
    driver.findElement(By.id("stock")).sendKeys("10");

    scrollToAndClick(By.name("save-component-button"));

    WebElement errorMessage = wait.until(
        ExpectedConditions.visibilityOfElementLocated(By.id("error-message")));
    assertThat(errorMessage.getText()).contains("Ya existe un componente con ese nombre.");
    assertThat(driver.getCurrentUrl()).isEqualTo("http://localhost:5173/components/create");

  }

  @Test
  public void createInvalidFormatComponentTest() {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components/create");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name")));
    driver.findElement(By.id("name")).sendKeys("testName");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("description")));
    driver.findElement(By.id("description")).sendKeys("testDescription");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("brand")));
    driver.findElement(By.id("brand")).sendKeys("testBrand");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("type")));
    Select select = new Select(driver.findElement(By.id("type")));
    select.selectByValue("CPU");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("price")));
    driver.findElement(By.id("price")).sendKeys("bad_format");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("stock")));
    driver.findElement(By.id("stock")).sendKeys("bad_format");

    scrollToAndClick(By.name("save-component-button"));
    
    assertThat(driver.findElement(By.id("invalid-price")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-price")).getText())
        .isEqualTo("Por favor, ingrese un precio válido.");
    assertThat(driver.findElement(By.id("invalid-stock")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-stock")).getText())
        .isEqualTo("Por favor, ingrese un stock válido.");
    assertThat(driver.getCurrentUrl()).isEqualTo("http://localhost:5173/components/create");

  }

  @Test
  public void createInvalidImageComponentTest() throws Exception {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components/create");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name")));
    driver.findElement(By.id("name")).sendKeys("testName");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("description")));
    driver.findElement(By.id("description")).sendKeys("testDescription");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("brand")));
    driver.findElement(By.id("brand")).sendKeys("testBrand");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("type")));
    Select select = new Select(driver.findElement(By.id("type")));
    select.selectByValue("CPU");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("price")));
    driver.findElement(By.id("price")).sendKeys("5");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("stock")));
    driver.findElement(By.id("stock")).sendKeys("5");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("image")));
    Path tempFile = Files.createTempFile("invalid-image", ".txt");
    Files.writeString(tempFile, "invalid file content");
    String imagePath = tempFile.toAbsolutePath().toString();
    driver.findElement(By.id("image")).sendKeys(imagePath);

    scrollToAndClick(By.name("save-component-button"));

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("error-message")));
    assertThat(driver.findElement(By.id("error-message")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("error-message")).getText())
        .isEqualTo("Hubo un error al subir la imagen. Solo se permiten imágenes JPEG, PNG y WebP.");
    assertThat(driver.getCurrentUrl()).isEqualTo("http://localhost:5173/components/create");

    Files.deleteIfExists(Paths.get(imagePath));

  }

  @Test
  public void cancelCreateComponentTest() {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components/create");

    scrollToAndClick(By.id("cancel-component-button"));

    wait.until(ExpectedConditions.urlToBe("http://localhost:5173/components"));

  }

  @Test
  public void deleteComponentTest() {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components/8");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("delete-component-button")));
    scrollToAndClick(By.id("delete-component-button"));
    WebElement deleteModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal.show")));
    assertThat(deleteModal.findElement(By.className("modal-title")).getText())
        .isEqualTo("¿Está seguro de que desea borrar este componente?");
    assertThat(deleteModal.findElement(By.className("modal-body")).getText())
        .contains("Toda su información se perderá y no se podrá recuperar.")
        .contains("Esta acción no se puede deshacer.");
    scrollToAndClick(By.cssSelector(".modal-footer .pcmod-danger-button"));

    driver.get("http://localhost:5173/components?page=0");
    assertThat(driver.getPageSource()).doesNotContain("Intel Core i5-12400F");

  }

  @Test
  public void cancelDeleteComponentTest() {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/components/1");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("delete-component-button")));
    scrollToAndClick(By.id("delete-component-button"));
    WebElement deleteModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal.show")));
    assertThat(deleteModal.findElement(By.className("modal-title")).getText())
        .isEqualTo("¿Está seguro de que desea borrar este componente?");
    assertThat(deleteModal.findElement(By.className("modal-body")).getText())
        .contains("Toda su información se perderá y no se podrá recuperar.")
        .contains("Esta acción no se puede deshacer.");
    scrollToAndClick(By.cssSelector(".modal-footer .btn-secondary"));

    driver.get("http://localhost:5173/components");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-1")));
    assertThat(driver.findElement(By.id("component-1")).getText())
      .isEqualTo("AMD Ryzen 7 7800X3D");

  }

  @Test
  public void cancelDeleteComponentClosingModalTest() {

    login("admin@example.com", "adminpass");
    
    driver.get("http://localhost:5173/components/1");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("delete-component-button")));
    scrollToAndClick(By.id("delete-component-button"));
    WebElement deleteModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal.show")));
    assertThat(deleteModal.findElement(By.className("modal-title")).getText())
        .isEqualTo("¿Está seguro de que desea borrar este componente?");
    assertThat(deleteModal.findElement(By.className("modal-body")).getText())
        .contains("Toda su información se perderá y no se podrá recuperar.")
        .contains("Esta acción no se puede deshacer.");
    scrollToAndClick(By.cssSelector(".modal-header .btn-close"));

    driver.get("http://localhost:5173/components");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-1")));
    assertThat(driver.findElement(By.id("component-1")).getText())
        .isEqualTo("AMD Ryzen 7 7800X3D");

  }
}