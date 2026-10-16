package es.codeurjcstudents.pcmod.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import javax.sql.rowset.serial.SerialBlob;
import java.util.List;
import java.util.NoSuchElementException;

import es.codeurjcstudents.pcmod.model.Image;
import es.codeurjcstudents.pcmod.model.User;
import es.codeurjcstudents.pcmod.repository.UsersRepository;
import es.codeurjcstudents.pcmod.service.ImageService;
import es.codeurjcstudents.pcmod.service.UsersService;

@Tag("server-unit")
public class UsersUnitTests {

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Test
  public void testCreateUser() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, null, null, null);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        passwordEncoder.encode("pass"), "REGISTERED_USER");

    when(usersRepository.save(user)).thenReturn(user);

    User actualUser = usersService.createUser(user);

    assertEquals(user, actualUser);
    verify(usersRepository).save(user);

  }

  @Test
  public void testGetUser() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, null, null, null);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        passwordEncoder.encode("pass"), "REGISTERED_USER");

    when(usersRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

    User actualUser = usersService.getUser(1L).orElseThrow();

    assertEquals(user, actualUser);
    verify(usersRepository).findById(1L);

  }

  @Test
  public void testGetUserException() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, null, null, null);

    when(usersRepository.findById(1L)).thenReturn(java.util.Optional.empty());

    assertThrows(NoSuchElementException.class, () -> {
      usersService.getUser(1L).orElseThrow();
    });

  }

  @Test
  public void testGetImageFile() throws Exception {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    Resource expectedImage = new ClassPathResource("/sample_images/user.png");
    Blob imageBlob = new SerialBlob(expectedImage.getInputStream().readAllBytes());
    Image image = new Image(imageBlob);
    when(usersRepository.findImageById(1L)).thenReturn(java.util.Optional.of(image));

    Resource actualImage = usersService.getImageFile(1L);

    assertArrayEquals(expectedImage.getInputStream().readAllBytes(), actualImage.getInputStream().readAllBytes());

  }

  @Test
  public void testGetVoidImageFile() throws SQLException {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    when(usersRepository.findImageById(1L)).thenReturn(java.util.Optional.empty());

    Resource image = usersService.getImageFile(1L);

    assertEquals("empty-profile-image.jpg", image.getFilename());

  }

  @Test
  public void testRegister() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        null, "");

    User userToSave = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        passwordEncoder.encode("R3gister_Test_Pass"), "REGISTERED_USER");
    when(usersRepository.save(any(User.class))).thenReturn(userToSave);

    User userSaved = usersService.register(user, "R3gister_Test_Pass");

    assertEquals(user.getUsername(), userSaved.getUsername());
    assertEquals(user.getName(), userSaved.getName());
    assertEquals(user.getSurname(), userSaved.getSurname());
    assertEquals(user.getAddress(), userSaved.getAddress());
    assertEquals(user.getEmail(), userSaved.getEmail());
    assertTrue(userSaved.getRoles().contains("REGISTERED_USER"));
    assertFalse(userSaved.getRoles().contains("ADMIN"));
    verify(usersRepository).save(any(User.class));

  }

  @Test
  public void testVoidCredentialsRegister() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    User user = new User("", "", "", "", "",
        null, "");

    assertThrows(IllegalArgumentException.class, () -> usersService.register(user, ""));
    verify(usersRepository, never()).save(any(User.class));

  }

  @Test
  public void testNullCredentialsRegister() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    User user = new User(null, null, null, null, null,
        null, "");

    assertThrows(IllegalArgumentException.class, () -> usersService.register(user, null));
    verify(usersRepository, never()).save(any(User.class));

  }

  @Test
  public void testExistingCredentialsRegister() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        null, "");

    when(usersRepository.existsByUsername("userTest")).thenReturn(true);
    when(usersRepository.existsByEmail("testuser@example.com")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> usersService.register(user, "R3gister_Existing"));
    verify(usersRepository, never()).save(any(User.class));

  }

  @Test
  public void testBadFormatCredentialsRegister() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    User user = new User("user", "test", "userTest", "c/testaddress", "test@email",
        null, "");

    assertThrows(IllegalArgumentException.class, () -> usersService.register(user, "pass"));
    verify(usersRepository, never()).save(any(User.class));
  }

  @Test
  public void testValidation() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        null, "");

    List<String> errors = usersService.validateUser(user, "R3gister_Test_Pass", null);

    assertTrue(errors.isEmpty());

  }

  @Test
  public void testVoidCredentialsValidation() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    User user = new User("", "", "", "", "",
        null, "");

    List<String> errors = usersService.validateUser(user, "", null);

    assertTrue(errors.contains("El nombre es obligatorio."));
    assertTrue(errors.contains("El apellido es obligatorio."));
    assertTrue(errors.contains("El nombre de usuario es obligatorio."));
    assertTrue(errors.contains("La dirección es obligatoria."));
    assertTrue(errors.contains("El email es obligatorio."));
    assertTrue(errors.contains("La contraseña es obligatoria."));

  }

  @Test
  public void testNullCredentialsValidation() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    User user = new User(null, null, null, null, null,
        null, "");

    List<String> errors = usersService.validateUser(user, null, null);

    assertTrue(errors.contains("El nombre es obligatorio."));
    assertTrue(errors.contains("El apellido es obligatorio."));
    assertTrue(errors.contains("El nombre de usuario es obligatorio."));
    assertTrue(errors.contains("La dirección es obligatoria."));
    assertTrue(errors.contains("El email es obligatorio."));
    assertTrue(errors.contains("La contraseña es obligatoria."));

  }

  @Test
  public void testExistingCredentialsValidation() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        null, "");

    when(usersRepository.existsByUsername("userTest")).thenReturn(true);
    when(usersRepository.existsByEmail("testuser@example.com")).thenReturn(true);

    List<String> errors = usersService.validateUser(user, "R3gister_Existing", null);

    assertTrue(errors.contains("El nombre de usuario ya existe."));
    assertTrue(errors.contains("El email ya existe."));
    verify(usersRepository).existsByUsername("userTest");
    verify(usersRepository).existsByEmail("testuser@example.com");

  }

  @Test
  public void testBadFormatCredentialsValidation() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    User user = new User("user", "test", "userTest", "c/testaddress", "test@email",
        null, "");

    List<String> errors = usersService.validateUser(user, "pass", null);

    assertTrue(errors.contains("El formato del email no es válido. Debe seguir el formato: ejemplo@dominio.com."));
    assertTrue(errors.contains(
        "La contraseña debe tener al menos 8 caracteres, incluir una mayúscula, una minúscula, un número y un carácter especial (@$!%*?&-_)."));

  }

  @Test
  public void testAddImageFile() throws Exception {

    UsersRepository usersRepository = mock(UsersRepository.class);
    ImageService imageService = mock(ImageService.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, imageService);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        passwordEncoder.encode("R3gister_Test_Pass"), "REGISTERED_USER");
    when(usersRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

    Resource expectedImage = new ClassPathResource("/sample_images/user.png");
    MockMultipartFile imageFile = new MockMultipartFile(
        "imageFile", "user.png", "image/png", expectedImage.getInputStream());
    Image image = new Image(new SerialBlob(expectedImage.getInputStream().readAllBytes()));
    when(imageService.createImage(any())).thenReturn(image);

    when(usersRepository.save(user)).thenReturn(user);

    Image actualImage = usersService.addUserImage(1L, imageFile);

    assertArrayEquals(expectedImage.getInputStream().readAllBytes(),
        actualImage.getImageFile().getBinaryStream().readAllBytes());

  }

  @Test
  public void testAddImageWithInvalidFile() throws IOException {

    UsersRepository usersRepository = mock(UsersRepository.class);
    ImageService imageService = mock(ImageService.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, imageService);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        passwordEncoder.encode("R3gister_Test_Pass"), "REGISTERED_USER");
    when(usersRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

    MockMultipartFile invalidTypeImage = new MockMultipartFile(
        "image", "image.txt", "text/plain", new byte[1024]);

    when(imageService.createImage(any())).thenThrow(IllegalArgumentException.class);

    assertThrows(IllegalArgumentException.class, () -> usersService.addUserImage(1L, invalidTypeImage));
  }

  @Test
  public void testUpdateImageFile() throws Exception {

    UsersRepository usersRepository = mock(UsersRepository.class);
    ImageService imageService = mock(ImageService.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, imageService);

    Resource originalResource = new ClassPathResource("/sample_images/user.png");
    MockMultipartFile originalImageFile = new MockMultipartFile("imageFile", "user.png", "image/png",
        originalResource.getInputStream());
    Image originalImage = new Image(new SerialBlob(originalImageFile.getInputStream().readAllBytes()));
    originalImage.setId(1L);

    Resource newResource = new ClassPathResource("/sample_images/user2.jpg");
    MockMultipartFile newImageFile = new MockMultipartFile("imageFile", "user2.jpg", "image/jpg",
        newResource.getInputStream());
    Image newImage = new Image(new SerialBlob(newImageFile.getInputStream().readAllBytes()));
    newImage.setId(2L);

    when(imageService.createImage(any(InputStream.class))).thenReturn(originalImage);
    when(imageService.replaceImageFile(anyLong(), any(InputStream.class))).thenReturn(newImage);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        passwordEncoder.encode("R3gister_Test_Pass"), "REGISTERED_USER");
    when(usersRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
    when(usersRepository.save(user)).thenReturn(user);
    usersService.addUserImage(1L, originalImageFile);

    Image actualImage = usersService.addUserImage(1L, newImageFile);

    verify(usersRepository, times(2)).save(user);

    assertArrayEquals(newResource.getInputStream().readAllBytes(),
        actualImage.getImageFile().getBinaryStream().readAllBytes());

  }

  @Test
  public void testDeleteUser() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, null, null, null);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        passwordEncoder.encode("pass"), "REGISTERED_USER");

    when(usersRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

    usersService.deleteUser(1L);

    verify(usersRepository).deleteById(1L);

  }

  @Test
  public void testDeleteUserNotFound() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, null, null, null);

    when(usersRepository.findById(1L)).thenReturn(java.util.Optional.empty());

    usersService.deleteUser(1L);

    verify(usersRepository, never()).deleteById(1L);

  }

  @Test
  public void testUpdate() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    User originalUser = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        passwordEncoder.encode("R3gister_Test_Pass"), "REGISTERED_USER");

    User updatedUser = new User("userUpdated", "testUpdated", "userTestUpdated", "c/testaddressUpdated",
        "testUpdateduser@example.com",
        passwordEncoder.encode("R3gister_Test_Pass"), "REGISTERED_USER");

    when(usersRepository.findById(1L)).thenReturn(java.util.Optional.of(originalUser));
    when(usersRepository.save(any(User.class))).thenReturn(originalUser);

    User userSaved = usersService.updateUser(updatedUser, "R3gister_Test_Pass", 1L);

    assertEquals(updatedUser.getUsername(), userSaved.getUsername());
    assertEquals(updatedUser.getName(), userSaved.getName());
    assertEquals(updatedUser.getSurname(), userSaved.getSurname());
    assertEquals(updatedUser.getAddress(), userSaved.getAddress());
    assertEquals(updatedUser.getEmail(), userSaved.getEmail());
    assertTrue(userSaved.getRoles().contains("REGISTERED_USER"));
    assertFalse(userSaved.getRoles().contains("ADMIN"));
    verify(usersRepository).save(any(User.class));

  }

  @Test
  public void testVoidCredentialsUpdate() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    User user = new User("", "", "", "", "",
        null, "");
    when(usersRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

    assertThrows(IllegalArgumentException.class, () -> usersService.updateUser(user, "", 1L));
    verify(usersRepository, never()).save(any(User.class));

  }

  @Test
  public void testNullCredentialsUpdate() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    User user = new User(null, null, null, null, null,
        null, "");
    when(usersRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

    assertThrows(IllegalArgumentException.class, () -> usersService.updateUser(user, null, 1L));
    verify(usersRepository, never()).save(any(User.class));

  }

  @Test
  public void testExistingCredentialsUpdate() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    User originalUser = new User("originalUser", "test", "originalUserTest", "c/testaddress",
        "originaltestuser@example.com", null, "");
    when(usersRepository.findById(1L)).thenReturn(java.util.Optional.of(originalUser));

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        null, "");

    when(usersRepository.existsByUsername("userTest")).thenReturn(true);
    when(usersRepository.existsByEmail("testuser@example.com")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> usersService.updateUser(user, "Updat3_Existing", 1L));
    verify(usersRepository, never()).save(any(User.class));

  }

  @Test
  public void testBadFormatCredentialsUpdate() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    User user = new User("user", "test", "userTest", "c/testaddress", "test@email",
        null, "");
    when(usersRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

    assertThrows(IllegalArgumentException.class, () -> usersService.updateUser(user, "pass", 1L));
    verify(usersRepository, never()).save(any(User.class));
  }

  @Test
  public void testUpdateKeepsExistingPasswordWhenEmpty() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);
    String encodedPassword = passwordEncoder.encode("Existing_Pass1");

    User originalUser = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        encodedPassword, "REGISTERED_USER");
    User updatedUser = new User("updated", "test", "userTest", "c/testaddress", "testuser@example.com",
        null, "REGISTERED_USER");

    when(usersRepository.findById(1L)).thenReturn(java.util.Optional.of(originalUser));
    when(usersRepository.save(any(User.class))).thenReturn(originalUser);

    usersService.updateUser(updatedUser, "", 1L);

    assertEquals(encodedPassword, originalUser.getEncodedPassword());
    verify(usersRepository).save(any(User.class));

  }

  @Test
  public void testUpdateValidation() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        null, "");
    when(usersRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

    List<String> errors = usersService.validateUser(user, "R3gister_Test_Pass", 1L);

    assertTrue(errors.isEmpty());

  }

  @Test
  public void testExistingCredentialsUpdateValidation() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    User originalUser = new User("originalUser", "test", "originalUserTest", "c/testaddress",
        "originaltestuser@example.com",
        null, "");
    when(usersRepository.findById(1L)).thenReturn(java.util.Optional.of(originalUser));

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        null, "");

    when(usersRepository.existsByUsername("userTest")).thenReturn(true);
    when(usersRepository.existsByEmail("testuser@example.com")).thenReturn(true);

    List<String> errors = usersService.validateUser(user, "R3gister_Existing", 1L);

    assertTrue(errors.contains("El nombre de usuario ya existe."));
    assertTrue(errors.contains("El email ya existe."));
    verify(usersRepository).existsByUsername("userTest");
    verify(usersRepository).existsByEmail("testuser@example.com");

  }

}
