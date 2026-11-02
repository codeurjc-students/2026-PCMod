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

  @Test
  public void loadHomePage() {

    driver.get("http://localhost:5173/");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("welcome")));
    String welcomeMessage = driver.findElement(By.id("welcome")).getText();
    assertThat(welcomeMessage).isEqualTo("Bienvenido a PCMod");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("logo")));
    String logoMessage = driver.findElement(By.id("logo")).getAttribute("alt");
    assertThat(logoMessage).isEqualTo("PCMod Logo");

    By recentComponentNames = By.cssSelector("h3[id^='component-']");
    wait.until(ExpectedConditions.numberOfElementsToBe(recentComponentNames, 3));
    assertThat(driver.findElements(recentComponentNames))
      .extracting(WebElement::getText)
      .containsExactly(
        "testComponent",
        "Corsair iCUE LINK 3500X RGB",
        "Kingston FURY Beast");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.name("componentsButton")));
    assertThat(driver.findElements(By.name("componentsButton"))).isNotEmpty();

  }
  
}