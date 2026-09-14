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
public class HeaderSystemTests {

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
  public void headerRenderTest() {

    driver.get("http://localhost:5173/");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("header")));

    assertThat(driver.findElement(By.className("logo")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.linkText("Componentes")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.linkText("Iniciar Sesión")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.linkText("Registrarse")).isDisplayed()).isTrue();

  }

  @Test
  public void loginButtonNavigatesToLoginTest() {

    driver.get("http://localhost:5173/");

    scrollToAndClick(By.linkText("Iniciar Sesión"));

    wait.until(ExpectedConditions.urlToBe("http://localhost:5173/login"));
    assertThat(driver.findElement(By.id("login-title")).getText()).isEqualTo("Iniciar sesión:");

  }

  @Test
  public void userDropdownOptionsTest() {

    login("user@example.com", "userpass");

    scrollToAndClick(By.id("dropdown-user"));
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("dropdown-menu")));

    assertThat(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("profile-option"))).isDisplayed())
        .isTrue();
    assertThat(driver.findElements(By.id("administration-option"))).isEmpty();
    assertThat(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logout-option"))).isDisplayed())
        .isTrue();

  }

  @Test
  public void adminDropdownOptionsTest() {

    login("admin@example.com", "adminpass");

    scrollToAndClick(By.id("dropdown-user"));
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("dropdown-menu")));

    assertThat(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("profile-option"))).isDisplayed())
        .isTrue();
    assertThat(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("administration-option")))
        .isDisplayed()).isTrue();
    assertThat(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logout-option"))).isDisplayed())
        .isTrue();

  }

  @Test
  public void logoutFromUserDropdownTest() {

    login("user@example.com", "userpass");

    scrollToAndClick(By.id("dropdown-user"));
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("dropdown-menu")));

    scrollToAndClick(By.id("logout-option"));

    wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Iniciar Sesión")));
    assertThat(driver.findElements(By.id("dropdown-user"))).isEmpty();
  }

  private void login(String email, String password) {

    driver.get("http://localhost:5173/login");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email"))).sendKeys(email);
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password"))).sendKeys(password);

    scrollToAndClick(By.name("login-button"));

    wait.until(ExpectedConditions.urlToBe("http://localhost:5173/"));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dropdown-user")));

  }

}