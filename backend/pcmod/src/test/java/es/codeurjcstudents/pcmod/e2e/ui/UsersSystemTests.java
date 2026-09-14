package es.codeurjcstudents.pcmod.e2e.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Tag("client-system")
public class UsersSystemTests {

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
  public void loginRenderTest() {

    driver.get("http://localhost:5173/login");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("login-title")));
    String loginTitle = driver.findElement(By.id("login-title")).getText();
    assertThat(loginTitle).isEqualTo("Iniciar sesión:");

    assertThat(driver.findElements(By.id("error-message"))).isEmpty();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email-title")));
    String emailTitle = driver.findElement(By.name("email-title")).getText();
    assertThat(emailTitle).isEqualTo("Correo electrónico:");
    assertThat(driver.findElements(By.id("email"))).isNotEmpty();
    assertThat(driver.findElement(By.id("invalid-email")).isDisplayed()).isFalse();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.name("password-title")));
    String passwordTitle = driver.findElement(By.name("password-title")).getText();
    assertThat(passwordTitle).isEqualTo("Contraseña:");
    assertThat(driver.findElements(By.id("password"))).isNotEmpty();
    assertThat(driver.findElement(By.id("invalid-password")).isDisplayed()).isFalse();

    assertThat(driver.findElements(By.name("login-button"))).isNotEmpty();
    assertThat(driver.findElements(By.id("register-link"))).isNotEmpty();

  }

  @Test
  public void loginVoidCredentialsTest() {

    driver.get("http://localhost:5173/login");

    scrollToAndClick(By.name("login-button"));

    assertThat(driver.findElement(By.id("invalid-email")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-email")).getText())
        .isEqualTo("Por favor, ingrese un correo electrónico válido.");

    assertThat(driver.findElement(By.id("invalid-password")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-password")).getText())
        .isEqualTo("Contraseña incorrecta. Inténtelo de nuevo.");

  }

  @Test
  public void loginValidCredentialsTest() {

    driver.get("http://localhost:5173/login");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
    driver.findElement(By.id("email")).sendKeys("user@example.com");
    driver.findElement(By.id("password")).sendKeys("userpass");

    scrollToAndClick(By.name("login-button"));

    assertThat(driver.findElement(By.id("invalid-email")).isDisplayed()).isFalse();
    assertThat(driver.findElement(By.id("invalid-password")).isDisplayed()).isFalse();

    wait.until(ExpectedConditions.urlToBe("http://localhost:5173/"));

  }

  @Test
  public void loginInvalidEmailFormatTest() {

    driver.get("http://localhost:5173/login");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
    driver.findElement(By.id("email")).sendKeys("user");
    driver.findElement(By.id("password")).sendKeys("userpass");

    scrollToAndClick(By.name("login-button"));

    assertThat(driver.findElement(By.id("invalid-email")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-email")).getText())
        .isEqualTo("Por favor, ingrese un correo electrónico válido.");
    assertThat(driver.getCurrentUrl()).isEqualTo("http://localhost:5173/login");

  }

  @Test
  public void loginInvalidPassFormatTest() {

    driver.get("http://localhost:5173/login");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
    driver.findElement(By.id("email")).sendKeys("user@example.com");
    driver.findElement(By.id("password")).sendKeys("pass");

    scrollToAndClick(By.name("login-button"));

    assertThat(driver.findElement(By.id("invalid-password")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-password")).getText())
        .isEqualTo("Contraseña incorrecta. Inténtelo de nuevo.");
    assertThat(driver.getCurrentUrl()).isEqualTo("http://localhost:5173/login");

  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
      "wrong email, userWrong@example.com, userpass",
      "wrong password, user@example.com, wrongPass",
      "wrong credentials, userWrong@example.com, wrongPass"
  })
  public void loginWrongCredentialsTest(String testName, String email, String password) {

    driver.get("http://localhost:5173/login");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
    driver.findElement(By.id("email")).sendKeys(email);
    driver.findElement(By.id("password")).sendKeys(password);

    scrollToAndClick(By.name("login-button"));

    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("error-message")));
    assertThat(driver.findElement(By.id("error-message")).getText())
        .isEqualTo("Error al iniciar sesión. Por favor, inténtenlo de nuevo");
    assertThat(driver.findElement(By.id("invalid-email")).isDisplayed()).isFalse();
    assertThat(driver.findElement(By.id("invalid-password")).isDisplayed()).isFalse();

  }

}