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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    List<String> errors = usersService.validateUser(user, "R3gister_Test_Pass");

    assertTrue(errors.isEmpty());

  }

  @Test
  public void testVoidCredentialsValidation() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    User user = new User("", "", "", "", "",
        null, "");

    List<String> errors = usersService.validateUser(user, "");

    assertTrue(errors.contains("Name is required"));
    assertTrue(errors.contains("Surname is required"));
    assertTrue(errors.contains("Username is required"));
    assertTrue(errors.contains("Email is required"));
    assertTrue(errors.contains("Password is required"));

  }

  @Test
  public void testNullCredentialsValidation() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    User user = new User(null, null, null, null, null,
        null, "");

    List<String> errors = usersService.validateUser(user, null);

    assertTrue(errors.contains("Name is required"));
    assertTrue(errors.contains("Surname is required"));
    assertTrue(errors.contains("Username is required"));
    assertTrue(errors.contains("Email is required"));
    assertTrue(errors.contains("Password is required"));

  }

  @Test
  public void testExistingCredentialsValidation() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        null, "");

    when(usersRepository.existsByUsername("userTest")).thenReturn(true);
    when(usersRepository.existsByEmail("testuser@example.com")).thenReturn(true);

    List<String> errors = usersService.validateUser(user, "R3gister_Existing");

    assertTrue(errors.contains("Username already exists"));
    assertTrue(errors.contains("Email already exists"));
    verify(usersRepository).existsByUsername("userTest");
    verify(usersRepository).existsByEmail("testuser@example.com");

  }

  @Test
  public void testBadFormatCredentialsValidation() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository, passwordEncoder, null, null);

    User user = new User("user", "test", "userTest", "c/testaddress", "test@email",
        null, "");

    List<String> errors = usersService.validateUser(user, "pass");

    assertTrue(errors.contains("Email format is invalid. It should be in the format: example@domain.com"));
    assertTrue(errors.contains(
        "Password must contain at least 8 characters including 1 uppercase letter, 1 lowercase letter, 1 digit, and 1 special character (@$!%*?&-_)"));

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
}
