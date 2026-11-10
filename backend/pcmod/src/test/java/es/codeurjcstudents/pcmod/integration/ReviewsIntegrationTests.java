package es.codeurjcstudents.pcmod.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import es.codeurjcstudents.pcmod.enums.ComponentType;
import es.codeurjcstudents.pcmod.model.Component;
import es.codeurjcstudents.pcmod.model.Review;
import es.codeurjcstudents.pcmod.model.User;
import es.codeurjcstudents.pcmod.repository.ComponentsRepository;
import es.codeurjcstudents.pcmod.repository.ReviewsRepository;
import es.codeurjcstudents.pcmod.repository.UsersRepository;
import es.codeurjcstudents.pcmod.service.ReviewsService;

@Tag("server-integration")
@SpringBootTest
@Testcontainers
public class ReviewsIntegrationTests {

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
  private ReviewsService reviewsService;

  @Autowired
  private ReviewsRepository reviewsRepository;

  @Autowired
  private ComponentsRepository componentsRepository;

  @Autowired
  private UsersRepository usersRepository;

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @BeforeEach
  void setUp() {
    reviewsRepository.deleteAll();
    componentsRepository.deleteAll();
    usersRepository.deleteAll();

    componentsRepository.save(new Component("AMD Ryzen 7 7800X3D",
        "8 núcleos y 16 hilos ideales para gaming",
        ComponentType.CPU, "AMD", BigDecimal.valueOf(350.00), 5));
    componentsRepository.save(new Component("Kingston NV3 1TB",
        "SSD de alta capacidad",
        ComponentType.STORAGE, "Kingston", BigDecimal.valueOf(100.00), 10));

    SecurityContextHolder.clearContext();
    usersRepository.save(new User("user1", "test", "userTest1", "c/testaddress", "testuser1@example.com",
        passwordEncoder.encode("pass"), "REGISTERED_USER"));
    usersRepository.save(new User("admin", "test", "adminTest1", "c/testaddress", "testadmin1@example.com",
        passwordEncoder.encode("adminpass"), "REGISTERED_USER", "ADMIN"));

    Review review1 = new Review("Buen procesador.",
    "Buen procesador para gaming.",
    5);
    reviewsService.createReview(usersRepository.findAll().getFirst().getId(), componentsRepository.findAll().getFirst().getId(), review1);
    Review review2 = new Review("Procesador un poco caro.",
    "Buen rendimiento pero precio elevado.", 
    3);
    reviewsService.createReview(usersRepository.findAll().get(1).getId(), componentsRepository.findAll().getFirst().getId(), review2);
  }

  @Test 
  void testCreateReview() {
    Review newReview = new Review("SSD de calidad", "Muy contento con la compra de este SSD", 4);
    Review createdReview = reviewsService.createReview(usersRepository.findAll().getFirst().getId(), componentsRepository.findAll().getLast().getId(), newReview);

    assertEquals(newReview.getTitle(), createdReview.getTitle());
    assertEquals(newReview.getMessage(), createdReview.getMessage());
    assertEquals(newReview.getRating(), createdReview.getRating());
    assertEquals(usersRepository.findAll().getFirst().getId(), createdReview.getUser().getId());
    assertEquals(componentsRepository.findAll().getLast().getId(), createdReview.getComponent().getId());
  }

  @Test 
  void testCreateReviewForNoExistingComponent() {
    Review newReview = new Review("SSD de calidad", "Muy contento con la compra de este SSD", 4);
   
    assertThrows(ResponseStatusException.class, () -> reviewsService.createReview(usersRepository.findAll().getFirst().getId(), componentsRepository.findAll().getLast().getId() + 1, newReview));
  }

  @Test
  void testCreateReviewForNoExistingUser() {
    Review newReview = new Review("SSD de calidad", "Muy contento con la compra de este SSD", 4);
   
    assertThrows(ResponseStatusException.class, () -> reviewsService.createReview(usersRepository.findAll().getLast().getId() + 1, componentsRepository.findAll().getLast().getId(), newReview));
  }

  @Test
  void testGetReviews() {
    Pageable pageable = Pageable.ofSize(10).withPage(0);

    Page<Review> reviewsList = reviewsService.findAllByComponent(componentsRepository.findAll().getFirst().getId(), pageable);

    Review firstReview = reviewsList.getContent().getFirst();
    Review lastReview = reviewsList.getContent().getLast();

    assertEquals(2, reviewsList.getNumberOfElements());
    assertEquals("Buen procesador.", firstReview.getTitle());
    assertEquals("Buen procesador para gaming.", firstReview.getMessage());
    assertEquals(5, firstReview.getRating());
    assertEquals(usersRepository.findAll().getFirst().getId(), firstReview.getUser().getId());
    assertEquals(componentsRepository.findAll().getFirst().getId(), firstReview.getComponent().getId());
    assertEquals("Procesador un poco caro.", lastReview.getTitle());
    assertEquals("Buen rendimiento pero precio elevado.", lastReview.getMessage());
    assertEquals(3, lastReview.getRating());
    assertEquals(usersRepository.findAll().getLast().getId(), lastReview.getUser().getId());
    assertEquals(componentsRepository.findAll().getFirst().getId(), lastReview.getComponent().getId());
  }

  @Test
  void testGetZeroReviews() {
    Pageable pageable = Pageable.ofSize(10).withPage(0);

    Page<Review> reviewsList = reviewsService.findAllByComponent(componentsRepository.findAll().getLast().getId(), pageable);

    assertTrue(reviewsList.getContent().isEmpty());
    assertEquals(0, reviewsList.getTotalElements());
    assertEquals(0, reviewsList.getTotalPages());

  }

  @Test
  void postReviewAsNoRegisteredUser() {
    Review review = new Review("sample_title", "sample_message", 5);
    assertThrows(AuthenticationCredentialsNotFoundException.class, () -> reviewsService.save(review, usersRepository.findAll().getFirst().getId(), componentsRepository.findAll().getLast().getId()));
    assertEquals(2, reviewsRepository.count());
  }

  @Test
  void postReviewAsNoAdmin() {
    User user = usersRepository.findByEmail("testuser1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"))));

    Review review = new Review("sample_title", "sample_message", 5);
    assertDoesNotThrow(() -> reviewsService.save(review, user.getId(), componentsRepository.findAll().getLast().getId()));
    assertEquals(3, reviewsRepository.count());
  }

  @Test
  void postReviewAsAdmin() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER", "ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Review review = new Review("sample_title", "sample_message", 5);
    assertDoesNotThrow(() -> reviewsService.save(review, admin.getId(), componentsRepository.findAll().getLast().getId()));
    assertEquals(3, reviewsRepository.count());

  }

  @Test
  void postReviewInAComponentWithOwnReview() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER", "ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Review review = new Review("sample_title", "sample_message", 5);
    assertThrows(IllegalArgumentException.class, () -> reviewsService.save(review, admin.getId(), componentsRepository.findAll().getFirst().getId()));
    assertEquals(2, reviewsRepository.count());

  }

  @Test
  void saveReview() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER", "ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Review review = new Review("sample_title", "sample_message", 5);
    Component component = componentsRepository.findAll().getLast();
    Review savedReview = reviewsService.save(review, admin.getId(), component.getId());

    assertEquals(review.getTitle(), savedReview.getTitle());
    assertEquals(review.getMessage(), savedReview.getMessage());
    assertEquals(review.getRating(), savedReview.getRating());
    assertEquals(component.getId(), savedReview.getComponent().getId());
    assertEquals(admin.getId(), savedReview.getUser().getId());
    assertEquals(3, reviewsRepository.count());

  }

  @Test
  void saveVoidReview() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER", "ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Review review = new Review("","", null);
    Component component = componentsRepository.findAll().getLast();

    assertThrows(IllegalArgumentException.class, () -> reviewsService.save(review, admin.getId(), component.getId()));
    assertEquals(2, reviewsRepository.count());
  }

  @Test
  void saveNullReview() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER", "ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Review review = new Review(null,null, null);
    Component component = componentsRepository.findAll().getLast();

    assertThrows(IllegalArgumentException.class, () -> reviewsService.save(review, admin.getId(), component.getId()));
    assertEquals(2, reviewsRepository.count());
  }

  @Test
  void saveBadFormatReview() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER", "ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Review review = new Review("sample_title", "sample_message", -5);
    Component component = componentsRepository.findAll().getLast();

    assertThrows(IllegalArgumentException.class, () -> reviewsService.save(review, admin.getId(), component.getId()));
    assertEquals(2, reviewsRepository.count());
  }

  @Test
  void validateReview() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER", "ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Review review = new Review("sample_title", "sample_message", 5);

    List<String> errors = reviewsService.validateReview(review);

    assertTrue(errors.isEmpty());

  }

  @Test
  void validateReviewInAComponentWithOwnReview() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER", "ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Review review = new Review("sample_title","sample_message", 5);
    Component component = componentsRepository.findAll().getFirst();

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> reviewsService.save(review, admin.getId(), component.getId()));
    assertEquals("Validación fallida:\n• Ya existe una reseña de este producto para tu usuario.", exception.getMessage());
    assertEquals(2, reviewsRepository.count());
  }

  @Test
  void validateVoidReview() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER", "ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Review review = new Review("", "", null);
   
    List<String> errors = reviewsService.validateReview(review);

    assertTrue(errors.contains("El título es obligatorio."));
    assertTrue(errors.contains("El mensaje es obligatorio."));
    assertTrue(errors.contains("La valoración es obligatoria."));

  }

  @Test
  void validateNullReview() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER", "ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Review review = new Review(null, null, null);

    List<String> errors = reviewsService.validateReview(review);

    assertTrue(errors.contains("El título es obligatorio."));
    assertTrue(errors.contains("El mensaje es obligatorio."));
    assertTrue(errors.contains("La valoración es obligatoria."));

  }

  @Test
  void validateBadNumReviewData() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER", "ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Review review = new Review("sample_title", "sample_message", -5);

    List<String> errors = reviewsService.validateReview(review);

    assertTrue(errors.contains("La valoración del producto ha de estar entre 1 y 5."));

  }

  @Test
  void deleteReviewAsNotLoggedIn() {

    Component component = componentsRepository.findAll().getFirst();
    Review review = reviewsRepository.findAll().getFirst();

    assertThrows(AuthenticationCredentialsNotFoundException.class,
        () -> reviewsService.delete(component.getId(), review.getId()));

  }

  @Test
  void deleteOwnReview() {
    User user = usersRepository.findByEmail("testuser1@example.com").orElseThrow();

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER").build(),
            null, List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"))));

    Component component = componentsRepository.findAll().getFirst();
    Review review = reviewsRepository.findAll().getFirst();

    assertDoesNotThrow(() -> reviewsService.delete(component.getId(), review.getId()));
    assertEquals(1, reviewsRepository.count());

  }

  @Test
  void deleteOtherReviewAsLoggedIn() {
    User user = usersRepository.findByEmail("testuser1@example.com").orElseThrow();

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER").build(),
            null, List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"))));

    Component component = componentsRepository.findAll().getFirst();
    Review review = reviewsRepository.findAll().getLast();

    assertThrows(AuthorizationDeniedException.class, () -> reviewsService.delete(component.getId(), review.getId()));
    assertEquals(2, reviewsRepository.count());

  }

  @Test
  void deleteReviewsAsAdmin() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Component component = componentsRepository.findAll().getFirst();
    Review reviewUser = reviewsRepository.findAll().getFirst();
    Review reviewAdmin = reviewsRepository.findAll().getLast();

    assertDoesNotThrow(() -> reviewsService.delete(component.getId(), reviewUser.getId()));
    assertDoesNotThrow(() -> reviewsService.delete(component.getId(), reviewAdmin.getId()));
    assertEquals(0, reviewsRepository.count());

  }

  @Test
  void deleteReviewOfWrongComponent() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Component component = componentsRepository.findAll().getLast();
    Review review = reviewsRepository.findAll().getFirst();

    assertThrows(NoSuchElementException.class, () -> reviewsService.delete(component.getId(), review.getId()));
    assertEquals(2, reviewsRepository.count());

  }

}
