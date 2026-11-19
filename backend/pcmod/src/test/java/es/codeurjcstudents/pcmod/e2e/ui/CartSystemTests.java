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
public class CartSystemTests {

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
  public void CartWithProductsRenderTest() {

    login("user@example.com", "userpass");

    driver.get("http://localhost:5173/cart");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("cart-title")));
    String cartTitle = driver.findElement(By.id("cart-title")).getText();
    assertThat(cartTitle).isEqualTo("Resumen del carro de compra:");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("cartItem-name-1")));
    String cartItemName = driver.findElement(By.id("cartItem-name-1")).getText();
    assertThat(cartItemName).isEqualTo("Seagate BarraCuda 3.5");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("cartItem-brand-type-1")));
    String cartItemBrandType = driver.findElement(By.id("cartItem-brand-type-1")).getText();
    assertThat(cartItemBrandType).isEqualTo("Seagate | STORAGE");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("cartItem-unit-price-1")));
    String cartItemUnitPrice = driver.findElement(By.id("cartItem-unit-price-1")).getText();
    assertThat(cartItemUnitPrice).isEqualTo("Precio unitario: 121.99 €/unidad");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("cartItem-price-1")));
    String cartItemPrice = driver.findElement(By.id("cartItem-price-1")).getText();
    assertThat(cartItemPrice).isEqualTo("121.99 €");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("go-to-payment-button")));
    String finalButtonText = driver.findElement(By.id("go-to-payment-button")).getText();
    assertThat(finalButtonText).isEqualTo("Finalizar compra");
    assertThat(driver.findElements(By.id("empty-cart"))).isEmpty();

  }

  @Test
  public void CartWithoutProductsRenderTest() {

    login("admin@example.com", "adminpass");

    driver.get("http://localhost:5173/cart");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("empty-cart")));
    String emptyCart = driver.findElement(By.id("empty-cart")).getText();
    assertThat(emptyCart).isEqualTo("No ha añadido productos al carro de compra todavía.");
    assertThat(driver.findElements(By.id("go-to-payment-button"))).isEmpty();

  }
  
}
