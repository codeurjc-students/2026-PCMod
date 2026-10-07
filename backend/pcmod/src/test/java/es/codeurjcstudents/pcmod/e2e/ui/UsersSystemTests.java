package es.codeurjcstudents.pcmod.e2e.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;

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
        .isEqualTo("Error al iniciar sesión. Por favor, inténtelo de nuevo");
    assertThat(driver.findElement(By.id("invalid-email")).isDisplayed()).isFalse();
    assertThat(driver.findElement(By.id("invalid-password")).isDisplayed()).isFalse();

  }

  @Test
  public void registerRenderTest() {

    driver.get("http://localhost:5173/register");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("register-title")));
    String registerTitle = driver.findElement(By.id("register-title")).getText();
    assertThat(registerTitle).isEqualTo("Registro:");

    assertThat(driver.findElements(By.id("error-message"))).isEmpty();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.name("name-title")));
    assertThat(driver.findElement(By.name("name-title")).getText()).isEqualTo("Nombre:");
    assertThat(driver.findElements(By.id("name"))).isNotEmpty();
    assertThat(driver.findElement(By.id("invalid-name")).isDisplayed()).isFalse();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.name("surname-title")));
    assertThat(driver.findElement(By.name("surname-title")).getText()).isEqualTo("Apellido:");
    assertThat(driver.findElements(By.id("surname"))).isNotEmpty();
    assertThat(driver.findElement(By.id("invalid-surname")).isDisplayed()).isFalse();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username-title")));
    assertThat(driver.findElement(By.name("username-title")).getText()).isEqualTo("Nombre de usuario:");
    assertThat(driver.findElements(By.id("username"))).isNotEmpty();
    assertThat(driver.findElement(By.id("invalid-username")).isDisplayed()).isFalse();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.name("address-title")));
    assertThat(driver.findElement(By.name("address-title")).getText()).isEqualTo("Dirección:");
    assertThat(driver.findElements(By.id("address"))).isNotEmpty();
    assertThat(driver.findElement(By.id("invalid-address")).isDisplayed()).isFalse();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email-title")));
    assertThat(driver.findElement(By.name("email-title")).getText()).isEqualTo("Correo electrónico:");
    assertThat(driver.findElements(By.id("email"))).isNotEmpty();
    assertThat(driver.findElement(By.id("invalid-email")).isDisplayed()).isFalse();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.name("password-title")));
    assertThat(driver.findElement(By.name("password-title")).getText()).isEqualTo("Contraseña:");
    assertThat(driver.findElements(By.id("password"))).isNotEmpty();
    assertThat(driver.findElement(By.id("invalid-password")).isDisplayed()).isFalse();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.name("image-title")));
    assertThat(driver.findElement(By.name("image-title")).getText()).isEqualTo("Imagen de perfil:");
    assertThat(driver.findElements(By.id("image"))).isNotEmpty();
    assertThat(driver.findElement(By.id("invalid-image")).isDisplayed()).isFalse();

    assertThat(driver.findElements(By.name("register-button"))).isNotEmpty();
    assertThat(driver.findElements(By.id("login-link"))).isNotEmpty();

  }

  @Test
  public void registerTest() throws Exception {

    driver.get("http://localhost:5173/register");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name")));
    driver.findElement(By.id("name")).sendKeys("user");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("surname")));
    driver.findElement(By.id("surname")).sendKeys("test");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
    driver.findElement(By.id("username")).sendKeys("userTest");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("address")));
    driver.findElement(By.id("address")).sendKeys("c/test");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
    driver.findElement(By.id("email")).sendKeys("user@test.com");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
    driver.findElement(By.id("password")).sendKeys("R3gister_Test");

    String imagePath = Files.createTempFile("valid-image", ".png").toString();
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("image")));
    driver.findElement(By.id("image")).sendKeys(imagePath);

    scrollToAndClick(By.name("register-button"));

    assertThat(driver.findElements(By.id("error-message"))).isEmpty();
    assertThat(driver.findElement(By.id("invalid-name")).isDisplayed()).isFalse();
    assertThat(driver.findElement(By.id("invalid-surname")).isDisplayed()).isFalse();
    assertThat(driver.findElement(By.id("invalid-username")).isDisplayed()).isFalse();
    assertThat(driver.findElement(By.id("invalid-address")).isDisplayed()).isFalse();
    assertThat(driver.findElement(By.id("invalid-email")).isDisplayed()).isFalse();
    assertThat(driver.findElement(By.id("invalid-password")).isDisplayed()).isFalse();
    assertThat(driver.findElement(By.id("invalid-image")).isDisplayed()).isFalse();

    wait.until(ExpectedConditions.urlToBe("http://localhost:5173/"));

    Files.deleteIfExists(Paths.get(imagePath));

  }

  @Test
  public void registerVoidCredentialsTest() {

    driver.get("http://localhost:5173/register");

    scrollToAndClick(By.name("register-button"));

    assertThat(driver.findElement(By.id("invalid-name")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-name")).getText())
        .isEqualTo("Por favor, ingrese un nombre válido.");
    assertThat(driver.findElement(By.id("invalid-surname")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-surname")).getText())
        .isEqualTo("Por favor, ingrese un apellido válido.");
    assertThat(driver.findElement(By.id("invalid-username")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-username")).getText())
        .isEqualTo("Por favor, ingrese un nombre de usuario válido.");
    assertThat(driver.findElement(By.id("invalid-address")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-address")).getText())
        .isEqualTo("Por favor, ingrese una dirección válida.");
    assertThat(driver.findElement(By.id("invalid-email")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-email")).getText())
        .isEqualTo("Por favor, ingrese un correo electrónico válido.");
    assertThat(driver.findElement(By.id("invalid-password")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("invalid-password")).getText())
        .isEqualTo("Por favor, ingrese una contraseña válida.");
    assertThat(driver.getCurrentUrl()).isEqualTo("http://localhost:5173/register");

  }

  @Test
  public void registerExistingCredentialsTest() {

    driver.get("http://localhost:5173/register");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name")));
    driver.findElement(By.id("name")).sendKeys("user");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("surname")));
    driver.findElement(By.id("surname")).sendKeys("test");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
    driver.findElement(By.id("username")).sendKeys("user_example");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("address")));
    driver.findElement(By.id("address")).sendKeys("c/test");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
    driver.findElement(By.id("email")).sendKeys("user@example.com");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
    driver.findElement(By.id("password")).sendKeys("R3gister_Test");

    scrollToAndClick(By.name("register-button"));

    WebElement errorMessage = wait.until(
        ExpectedConditions.visibilityOfElementLocated(By.id("error-message")));
    assertThat(errorMessage.getText()).contains("El nombre de usuario ya existe.");
    assertThat(errorMessage.getText()).contains("El email ya existe.");
    assertThat(driver.getCurrentUrl()).isEqualTo("http://localhost:5173/register");

  }

  @Test
  public void registerInvalidFormatCredentialsTest() {

    driver.get("http://localhost:5173/register");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name")));
    driver.findElement(By.id("name")).sendKeys("user");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("surname")));
    driver.findElement(By.id("surname")).sendKeys("test");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
    driver.findElement(By.id("username")).sendKeys("userTest");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("address")));
    driver.findElement(By.id("address")).sendKeys("c/test");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
    driver.findElement(By.id("email")).sendKeys("user@example");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
    driver.findElement(By.id("password")).sendKeys("TestPass");

    scrollToAndClick(By.name("register-button"));

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("error-message")));
    assertThat(driver.findElement(By.id("error-message")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("error-message")).getText())
        .contains("El formato del email no es válido. Debe seguir el formato: ejemplo@dominio.com.");
    assertThat(driver.findElement(By.id("error-message")).getText()).contains(
        "La contraseña debe tener al menos 8 caracteres, incluir una mayúscula, una minúscula, un número y un carácter especial (@$!%*?&-_).");
    assertThat(driver.getCurrentUrl()).isEqualTo("http://localhost:5173/register");

  }

  @Test
  public void registerInvalidImageFormatTest() throws Exception {

    driver.get("http://localhost:5173/register");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name")));
    driver.findElement(By.id("name")).sendKeys("user");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("surname")));
    driver.findElement(By.id("surname")).sendKeys("test");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
    driver.findElement(By.id("username")).sendKeys("userTestImage");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("address")));
    driver.findElement(By.id("address")).sendKeys("c/test");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
    driver.findElement(By.id("email")).sendKeys("user@testImage.com");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
    driver.findElement(By.id("password")).sendKeys("R3gister_Test");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("image")));
    Path tempFile = Files.createTempFile("invalid-image", ".txt");
    Files.writeString(tempFile, "invalid file content");
    String imagePath = tempFile.toAbsolutePath().toString();
    driver.findElement(By.id("image")).sendKeys(imagePath);

    scrollToAndClick(By.name("register-button"));

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("error-message")));
    assertThat(driver.findElement(By.id("error-message")).isDisplayed()).isTrue();
    assertThat(driver.findElement(By.id("error-message")).getText())
        .isEqualTo("Hubo un error al subir la imagen. Solo se permiten imágenes JPEG, PNG y WebP.");
    assertThat(driver.getCurrentUrl()).isEqualTo("http://localhost:5173/register");

    Files.deleteIfExists(Paths.get(imagePath));

  }

  @Test
  public void loadProfilePageTest() {

    driver.get("http://localhost:5173/login");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
    driver.findElement(By.id("email")).sendKeys("user@example.com");
    driver.findElement(By.id("password")).sendKeys("userpass");

    scrollToAndClick(By.name("login-button"));
    wait.until(ExpectedConditions.urlToBe("http://localhost:5173/"));

    driver.get("http://localhost:5173/me");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name")));
    String loadedName = driver.findElement(By.id("name")).getText();
    assertThat(loadedName).isEqualTo("user");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("surname")));
    String loadedSurname = driver.findElement(By.id("surname")).getText();
    assertThat(loadedSurname).isEqualTo("example");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
    String loadedUsername = driver.findElement(By.id("username")).getText();
    assertThat(loadedUsername).isEqualTo("user_example");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("address")));
    String loadedAddress = driver.findElement(By.id("address")).getText();
    assertThat(loadedAddress).isEqualTo("c/example_address 1");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
    String loadedEmail = driver.findElement(By.id("email")).getText();
    assertThat(loadedEmail).isEqualTo("user@example.com");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("purchases")));
    String loadedPurchasesTitle = driver.findElement(By.id("purchases")).getText();
    assertThat(loadedPurchasesTitle).isEqualTo("Mis compras:");

  }

  @Test
  public void cancelAccountDeletionTest() {

    driver.get("http://localhost:5173/login");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
    driver.findElement(By.id("email")).sendKeys("user@example.com");
    driver.findElement(By.id("password")).sendKeys("userpass");
    scrollToAndClick(By.name("login-button"));
    wait.until(ExpectedConditions.urlToBe("http://localhost:5173/"));

    driver.get("http://localhost:5173/me");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.name("delete-button")));
    scrollToAndClick(By.name("delete-button"));

    WebElement deleteModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal.show")));
    assertThat(deleteModal.findElement(By.className("modal-title")).getText())
        .isEqualTo("¿Está seguro de que desea borrar su cuenta?");
    assertThat(deleteModal.findElement(By.className("modal-body")).getText())
        .contains("Todos sus datos se perderán y no podrá recuperarlos.")
        .contains("Esta acción no se puede deshacer.");

    scrollToAndClick(By.cssSelector(".modal-footer .btn-secondary"));

    wait.until(ExpectedConditions.urlToBe("http://localhost:5173/me"));

  }

  @Test
  public void cancelAccountDeletionClosingModalTest() {

    driver.get("http://localhost:5173/login");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
    driver.findElement(By.id("email")).sendKeys("user@example.com");
    driver.findElement(By.id("password")).sendKeys("userpass");
    scrollToAndClick(By.name("login-button"));
    wait.until(ExpectedConditions.urlToBe("http://localhost:5173/"));

    driver.get("http://localhost:5173/me");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.name("delete-button")));
    scrollToAndClick(By.name("delete-button"));

    WebElement deleteModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal.show")));
    assertThat(deleteModal.findElement(By.className("modal-title")).getText())
        .isEqualTo("¿Está seguro de que desea borrar su cuenta?");
    assertThat(deleteModal.findElement(By.className("modal-body")).getText())
        .contains("Todos sus datos se perderán y no podrá recuperarlos.")
        .contains("Esta acción no se puede deshacer.");
    scrollToAndClick(By.cssSelector(".modal-header .btn-close"));

    wait.until(ExpectedConditions.urlToBe("http://localhost:5173/me"));

  }

  @Test
  public void deleteAccountTest() {

    driver.get("http://localhost:5173/register");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name")));
    driver.findElement(By.id("name")).sendKeys("Delete");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("surname")));
    driver.findElement(By.id("surname")).sendKeys("Test");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
    driver.findElement(By.id("username")).sendKeys("delete_test");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("address")));
    driver.findElement(By.id("address")).sendKeys("c/delete_test");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
    driver.findElement(By.id("email")).sendKeys("delete_test@example.com");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
    driver.findElement(By.id("password")).sendKeys("Delete_Test1");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.name("register-button")));
    scrollToAndClick(By.name("register-button"));
    wait.until(ExpectedConditions.urlToBe("http://localhost:5173/"));

    driver.get("http://localhost:5173/me");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.name("delete-button")));
    scrollToAndClick(By.name("delete-button"));

    WebElement deleteModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal.show")));
    assertThat(deleteModal.findElement(By.className("modal-title")).getText())
        .isEqualTo("¿Está seguro de que desea borrar su cuenta?");
    assertThat(deleteModal.findElement(By.className("modal-body")).getText())
        .contains("Todos sus datos se perderán y no podrá recuperarlos.")
        .contains("Esta acción no se puede deshacer.");
    scrollToAndClick(By.cssSelector(".modal-footer .pcmod-btn-danger"));

    wait.until(ExpectedConditions.urlToBe("http://localhost:5173/"));

    driver.get("http://localhost:5173/login");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
    driver.findElement(By.id("email")).sendKeys("delete_test@example.com");
    driver.findElement(By.id("password")).sendKeys("Delete_Test1");
    scrollToAndClick(By.name("login-button"));

    WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("error-message")));
    assertThat(errorMessage.getText()).isEqualTo("Error al iniciar sesión. Por favor, inténtelo de nuevo");

  }
}