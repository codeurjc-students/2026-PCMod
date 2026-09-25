package es.codeurjcstudents.pcmod.integration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import es.codeurjcstudents.pcmod.model.Image;
import es.codeurjcstudents.pcmod.model.User;
import es.codeurjcstudents.pcmod.repository.UsersRepository;
import es.codeurjcstudents.pcmod.service.UsersService;

@Tag("server-integration")
@SpringBootTest
@Testcontainers
public class UsersIntegrationTests {

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Container
  private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.4")
      .withDatabaseName("TestDB")
      .withUsername("TestDBUser")
      .withPassword("TestDBPassword");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url",
        () -> mysqlContainer.getJdbcUrl() + "?useSSL=false&allowPublicKeyRetrieval=true");

    registry.add("spring.datasource.username", mysqlContainer::getUsername);
    registry.add("spring.datasource.password", mysqlContainer::getPassword);
    registry.add("spring.datasource.driver-class-name", mysqlContainer::getDriverClassName);
  }

  @Autowired
  private UsersService usersService;

  @Autowired
  private UsersRepository usersRepository;

  @BeforeEach
  void setUp() {
    usersRepository.deleteAll();
    usersRepository.save(new User("user1", "test", "userTest1", "c/testaddress", "testuser1@example.com",
        passwordEncoder.encode("pass"), "REGISTERED_USER"));
    usersRepository.save(new User("admin", "test", "adminTest1", "c/testaddress", "testadmin1@example.com",
        passwordEncoder.encode("adminpass"), "REGISTERED_USER", "ADMIN"));
  }

  @Test
  void createUser() {
    User user = new User("user3", "test", "userTest3", "c/testaddress", "testuser3@example.com",
        passwordEncoder.encode("pass"), "REGISTERED_USER");

    User createdUser = usersService.createUser(user);

    assertEquals("user3", createdUser.getName());
    assertEquals("test", createdUser.getSurname());
    assertEquals("userTest3", createdUser.getUsername());
    assertEquals("c/testaddress", createdUser.getAddress());
    assertEquals("testuser3@example.com", createdUser.getEmail());

  }

  @Test
  void getUser() {
    User user = usersRepository.save(new User("user3", "test", "userTest3", "c/testaddress", "testuser3@example.com",
        passwordEncoder.encode("pass"), "REGISTERED_USER"));

    User loadedUser = usersService.getUser(user.getId()).orElseThrow();

    assertEquals("user3", loadedUser.getName());
    assertEquals("test", loadedUser.getSurname());
    assertEquals("userTest3", loadedUser.getUsername());
    assertEquals("c/testaddress", loadedUser.getAddress());
    assertEquals("testuser3@example.com", loadedUser.getEmail());

  }

  @Test
  void getOwnImage() throws IOException, SQLException {
    Resource expectedImage = new ClassPathResource("/sample_images/user.png");
    MockMultipartFile imageFile = new MockMultipartFile("imageFile", "user.png", "image/png",
        expectedImage.getInputStream());

    User user = usersRepository.findByEmail("testuser1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"))));
    usersService.addUserImage(user.getId(), imageFile);

    Resource actualImage = usersService.getImageFile(user.getId());

    assertArrayEquals(expectedImage.getInputStream().readAllBytes(), actualImage.getInputStream().readAllBytes());

  }

  @Test
  void getOwnVoidImage() throws IOException, SQLException {
    Resource expectedImage = new ClassPathResource("/sample_images/empty-profile-image.jpg");

    User user = usersRepository.findByEmail("testuser1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"))));

    Resource actualImage = usersService.getImageFile(user.getId());

    assertArrayEquals(expectedImage.getInputStream().readAllBytes(), actualImage.getInputStream().readAllBytes());
  }

  @Test
  void getOtherUserImageAsNoAdmin() {
    User user = usersRepository.findByEmail("testuser1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"))));

    assertThrows(AuthorizationDeniedException.class, () -> usersService.getImageFile(2L));
  }

  @Test
  void getImagesAsAdmin() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    User user = usersRepository.findByEmail("testuser1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    assertDoesNotThrow(() -> usersService.getImageFile(user.getId()));
    assertDoesNotThrow(() -> usersService.getImageFile(admin.getId()));
  }

  @Test
  void register() {
    User newUser = new User("user", "test", "userTest2", "c/testAddress", "testuser2@example.com", null,
        "REGISTERED_USER");
    User savedUser = usersService.register(newUser, "R3gister_Test_Pass");

    assertEquals(newUser.getName(), savedUser.getName());
    assertEquals(newUser.getSurname(), savedUser.getSurname());
    assertEquals(newUser.getUsername(), savedUser.getUsername());
    assertEquals(newUser.getAddress(), savedUser.getAddress());
    assertEquals(newUser.getEmail(), savedUser.getEmail());
    assertTrue(savedUser.getRoles().contains("REGISTERED_USER"));
    assertFalse(savedUser.getRoles().contains("ADMIN"));
    assertEquals(3, usersRepository.count());

  }

  @Test
  void registerVoidCredentials() {
    User newUser = new User("", "", "", "", "", "", "");

    assertThrows(IllegalArgumentException.class, () -> usersService.register(newUser, ""));
    assertEquals(2, usersRepository.count());
  }

  @Test
  void registerNullCredentials() {
    User newUser = new User(null, null, null, null, null, null, "");

    assertThrows(IllegalArgumentException.class, () -> usersService.register(newUser, null));
    assertEquals(2, usersRepository.count());
  }

  @Test
  void registerExistingCredentials() {
    User newUser = new User("user", "test", "userTest1", "c/testAddress", "testuser1@example.com", null,
        "REGISTERED_USER");

    assertThrows(IllegalArgumentException.class, () -> usersService.register(newUser, "R3gister_Test_Pass"));
    assertEquals(2, usersRepository.count());
  }

  @Test
  void registerBadFormatCredentials() {
    User newUser = new User("user", "test", "userTest", "c/testAddress", "test@example", null,
        "REGISTERED_USER");

    assertThrows(IllegalArgumentException.class, () -> usersService.register(newUser, "pass"));
    assertEquals(2, usersRepository.count());
  }

  @Test
  void validateUser() {
    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        null, "");

    List<String> errors = usersService.validateUser(user, "R3gister_Test_Pass");

    assertTrue(errors.isEmpty());

  }

  @Test
  void validateVoidCredentials() {
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
  void validateNullCredentials() {
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
  void validateExistingCredentials() {
    User user = new User("user", "test", "userTest1", "c/testaddress", "testuser1@example.com",
        null, "");

    List<String> errors = usersService.validateUser(user, "R3gister_Test_Pass");

    assertTrue(errors.contains("Username already exists"));
    assertTrue(errors.contains("Email already exists"));

  }

  @Test
  void validateBadFormatCredentials() {
    User user = new User("user", "test", "userTest", "c/testaddress", "test@email",
        null, "");

    List<String> errors = usersService.validateUser(user, "pass");

    assertTrue(errors.contains("Email format is invalid. It should be in the format: example@domain.com"));
    assertTrue(errors.contains(
        "Password must contain at least 8 characters including 1 uppercase letter, 1 lowercase letter, 1 digit, and 1 special character (@$!%*?&-_)"));

  }

  @Test
  void addOwnImage() throws IOException, SQLException {
    User user = usersRepository.findByEmail("testuser1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"))));

    Resource expectedImage = new ClassPathResource("/sample_images/user.png");
    MockMultipartFile imageFile = new MockMultipartFile("imageFile", "user.png", "image/png",
        expectedImage.getInputStream());

    Image savedImage = usersService.addUserImage(user.getId(), imageFile);

    assertArrayEquals(expectedImage.getInputStream().readAllBytes(),
        savedImage.getImageFile().getBinaryStream().readAllBytes());

  }

  @Test
  void addOtherUserImageAsNoAdmin() throws IOException {
    User user = usersRepository.findByEmail("testuser1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"))));

    Resource expectedImage = new ClassPathResource("/sample_images/user.png");
    MockMultipartFile imageFile = new MockMultipartFile("imageFile", "user.png", "image/png",
        expectedImage.getInputStream());

    assertThrows(AuthorizationDeniedException.class, () -> usersService.addUserImage(2L, imageFile));

  }

  @Test
  void addImagesAsAdmin() throws IOException, SQLException {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    User user = usersRepository.findByEmail("testuser1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Resource expectedImage = new ClassPathResource("/sample_images/user.png");
    MockMultipartFile imageFile = new MockMultipartFile("imageFile", "user.png", "image/png",
        expectedImage.getInputStream());

    Image savedUserImage = usersService.addUserImage(user.getId(), imageFile);

    assertArrayEquals(expectedImage.getInputStream().readAllBytes(),
        savedUserImage.getImageFile().getBinaryStream().readAllBytes());

    Image savedAdminImage = usersService.addUserImage(admin.getId(), imageFile);

    assertArrayEquals(expectedImage.getInputStream().readAllBytes(),
        savedAdminImage.getImageFile().getBinaryStream().readAllBytes());

  }

}
