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
  public void loadComponentsTest() throws InterruptedException {

    driver.get("http://localhost:5173/components");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name-1")));
    String componentName = driver.findElement(By.id("name-1")).getText();
    assertThat(componentName).isEqualTo("AMD Ryzen 7 7800X3D");

    assertThat(driver.findElements(By.id("name-11"))).isEmpty();

    WebElement loadMoreButton = driver.findElement(By.name("loadMore"));
    loadMoreButton.click();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name-11")));
    String loadedComponentName = driver.findElement(By.id("name-11")).getText();
    assertThat(loadedComponentName).isEqualTo("Kingston FURY Beast");

    assertThat(driver.findElements(By.name("loadMore"))).isEmpty();

  }

  @Test
  public void loadErrorTest() {

    driver.get("http://localhost:5173/components");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.name("loadMore")));
    WebElement loadMoreButton = driver.findElement(By.name("loadMore"));

    // Simulate a network error
    ((ChromeDriver) driver).executeCdpCommand("Network.enable", Map.of());
    ((ChromeDriver) driver).executeCdpCommand("Network.emulateNetworkConditions", Map.of(
        "offline", true,
        "latency", 0,
        "downloadThroughput", 0,
        "uploadThroughput", 0));

    loadMoreButton.click();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[role='alert']")));
    String errorMessage = driver.findElement(By.cssSelector("[role='alert']")).getText();
    assertThat(errorMessage).isEqualTo("Hubo un problema al cargar más componentes. Por favor, pruebe de nuevo.");

  }
}