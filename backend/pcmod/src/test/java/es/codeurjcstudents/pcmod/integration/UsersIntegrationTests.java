package es.codeurjcstudents.pcmod.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
    usersRepository.save(new User("user2", "test", "userTest2", "c/testaddress", "testuser2@example.com",
        passwordEncoder.encode("pass"), "REGISTERED_USER"));
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

}
