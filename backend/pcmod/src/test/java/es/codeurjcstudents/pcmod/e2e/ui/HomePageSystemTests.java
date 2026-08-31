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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Tag("client-system")
public class HomePageSystemTests {

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

    driver = new ChromeDriver(options);
    wait = new WebDriverWait(driver, Duration.ofSeconds(10));
  }

  @AfterEach
  public void teardown() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  public void loadHomePage() throws InterruptedException {

    driver.get("http://localhost:5173/");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("welcome")));
    String welcomeMessage = driver.findElement(By.id("welcome")).getText();
    assertThat(welcomeMessage).isEqualTo("Bienvenido a PCMod");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("logo")));
    String logoMessage = driver.findElement(By.id("logo")).getAttribute("alt");
    assertThat(logoMessage).isEqualTo("PCMod Logo");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-9")));
    String componentName9 = driver.findElement(By.id("component-9")).getText();
    assertThat(componentName9).isEqualTo("AMD Radeon RX 9060 XT DUAL WHITE");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-10")));
    String componentName10 = driver.findElement(By.id("component-10")).getText();
    assertThat(componentName10).isEqualTo("Seagate BarraCuda 3.5");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("component-11")));
    String componentName11 = driver.findElement(By.id("component-11")).getText();
    assertThat(componentName11).isEqualTo("Kingston FURY Beast");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.name("componentsButton")));
    assertThat(driver.findElements(By.name("componentsButton"))).isNotEmpty();

  }

  @Test
  public void navigateToComponentsPage() throws InterruptedException {

    driver.get("http://localhost:5173/");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.name("componentsButton")));
    WebElement componentsButton = driver.findElement(By.name("componentsButton"));
    componentsButton.click();

    wait.until(ExpectedConditions.urlToBe("http://localhost:5173/components"));

  }
}
