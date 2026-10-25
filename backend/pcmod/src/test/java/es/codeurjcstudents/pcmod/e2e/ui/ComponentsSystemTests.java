package es.codeurjcstudents.pcmod.e2e.ui;

import static org.assertj.core.api.Assertions.assertThat;

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

    driver.get("http://localhost:5173/login");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
    driver.findElement(By.id("email")).sendKeys("user@example.com");
    driver.findElement(By.id("password")).sendKeys("userpass");

    scrollToAndClick(By.name("login-button"));

    wait.until(ExpectedConditions.urlToBe("http://localhost:5173/"));

    driver.get("http://localhost:5173/components/1");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("add-to-cart-button")));

    assertThat(driver.findElements(By.id("add-to-cart-button"))).isNotEmpty();
    assertThat(driver.findElements(By.id("edit-component-button"))).isEmpty();
    assertThat(driver.findElements(By.id("delete-component-button"))).isEmpty();

  }

  @Test
  public void componentDetailOptionsAsAdminTest() {

    driver.get("http://localhost:5173/login");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
    driver.findElement(By.id("email")).sendKeys("admin@example.com");
    driver.findElement(By.id("password")).sendKeys("adminpass");

    scrollToAndClick(By.name("login-button"));

    wait.until(ExpectedConditions.urlToBe("http://localhost:5173/"));

    driver.get("http://localhost:5173/components/1");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("add-to-cart-button")));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("edit-component-button")));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("delete-component-button")));

    assertThat(driver.findElements(By.id("add-to-cart-button"))).isNotEmpty();
    assertThat(driver.findElements(By.id("edit-component-button"))).isNotEmpty();
    assertThat(driver.findElements(By.id("delete-component-button"))).isNotEmpty();

  }
}