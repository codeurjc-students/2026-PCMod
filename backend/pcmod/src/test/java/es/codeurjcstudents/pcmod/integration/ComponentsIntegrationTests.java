package es.codeurjcstudents.pcmod.integration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
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
import es.codeurjcstudents.pcmod.model.Image;
import es.codeurjcstudents.pcmod.model.User;
import es.codeurjcstudents.pcmod.repository.ComponentsRepository;
import es.codeurjcstudents.pcmod.repository.UsersRepository;
import es.codeurjcstudents.pcmod.service.ComponentsService;
import es.codeurjcstudents.pcmod.service.ImageService;

@Tag("server-integration")
@SpringBootTest
@Testcontainers
public class ComponentsIntegrationTests {

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
  private ComponentsService componentsService;

  @Autowired
  private ComponentsRepository componentsRepository;

  @Autowired
  private ImageService imageService;

  @Autowired
  private UsersRepository usersRepository;

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @BeforeEach
  void setUp() {
    componentsRepository.deleteAll();
    componentsRepository.save(new Component("AMD Ryzen 7 7800X3D",
        "8 núcleos y 16 hilos ideales para gaming\r\n" + //
            "96 MB de caché L3 3D V-Cache, baja latencia\r\n" + //
            "Compatible con memorias DDR5, doble canal\r\n" + //
            "Frecuencia base 4.2 GHz y turbo hasta 5.0 GHz",
        ComponentType.CPU, "AMD", BigDecimal.valueOf(350.00), 5));
    componentsRepository.save(new Component("NVIDIA GeForce RTX 5060 WINDFORCE MAX OC",
        "Ray Tracing y DLSS 4 para gaming fluido\r\n" + //
            "8GB GDDR7 y arquitectura Blackwell\r\n" + //
            "WINDFORCE 2X: refrigeración eficiente y silenciosa\r\n" + //
            "DisplayPort 2.1 y HDMI 2.1b hasta 8K\r\n" + //
            "Tensor Cores 5ª gen y Reflex 2 para IA avanzada",
        ComponentType.GPU, "Gigabyte", BigDecimal.valueOf(359.85), 2));

    SecurityContextHolder.clearContext();
    usersRepository.deleteAll();
    usersRepository.save(new User("user1", "test", "userTest1", "c/testaddress", "testuser1@example.com",
        passwordEncoder.encode("pass"), "REGISTERED_USER"));
    usersRepository.save(new User("admin", "test", "adminTest1", "c/testaddress", "testadmin1@example.com",
        passwordEncoder.encode("adminpass"), "REGISTERED_USER", "ADMIN"));

  }

  @Test
  void loadComponents() {
    Pageable pageable = Pageable.ofSize(10).withPage(0);

    Page<Component> componentList = componentsService.findAll(pageable);

    assertEquals(2, componentList.getNumberOfElements());
    assertEquals("AMD Ryzen 7 7800X3D", componentList.getContent().get(0).getName());
    assertEquals("NVIDIA GeForce RTX 5060 WINDFORCE MAX OC", componentList.getContent().get(1).getName());
  }

  @Test
  public void testGetComponent() {
    Component component = componentsRepository.save(new Component("Intel i5 12400F",
        "Procesador de 6 núcleos y 12 hilos", ComponentType.CPU, "Intel", BigDecimal.valueOf(153.97), 14));

    Component loadedComponent = componentsService.getComponent(component.getId());

    assertDoesNotThrow(() -> componentsService.getComponent(component.getId()));
    assertEquals(component.getId(), loadedComponent.getId());
    assertEquals(component.getName(), loadedComponent.getName());
    assertEquals(component.getDescription(), loadedComponent.getDescription());
    assertEquals(component.getType(), loadedComponent.getType());
    assertEquals(component.getBrand(), loadedComponent.getBrand());
    assertEquals(component.getPrice(), loadedComponent.getPrice());
    assertEquals(component.getStock(), loadedComponent.getStock());
  }

  @Test
  public void testGetComponentNotFound() {
    assertThrows(ResponseStatusException.class, () -> componentsService.getComponent(99L));
  }

  @Test
  public void testGetComponentImage() throws IOException, SQLException {
    Component component = componentsRepository.findAll().get(0);
    Resource expectedImage = new ClassPathResource("/sample_images/ryzen-7.webp");
    component.setImage(imageService.createImage(expectedImage.getInputStream()));
    componentsRepository.save(component);
    Resource actualImage = componentsService.getImageFile(component.getId());

    assertArrayEquals(expectedImage.getInputStream().readAllBytes(), actualImage.getInputStream().readAllBytes());
  }

  @Test
  public void testGetNullComponentImage() throws SQLException {
    Resource actualImage = componentsService.getImageFile(1L);

    assertEquals(null, actualImage);
  }

  @Test
  void postComponentAsNoRegisteredUser() {
    Component component = new Component("Intel i5 12400F", "Procesador de 6 núcleos y 12 hilos", ComponentType.CPU,
        "Intel", BigDecimal.valueOf(153.97), 14);
    assertThrows(AuthenticationCredentialsNotFoundException.class, () -> componentsService.save(component));
  }

  @Test
  void postComponentAsNoAdmin() {
    User user = usersRepository.findByEmail("testuser1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"))));

    Component component = new Component("Intel i5 12400F", "Procesador de 6 núcleos y 12 hilos", ComponentType.CPU,
        "Intel", BigDecimal.valueOf(153.97), 14);
    assertThrows(AuthorizationDeniedException.class, () -> componentsService.save(component));

  }

  @Test
  void postComponentAsAdmin() {
    User user = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Component component = new Component("Intel i5 12400F", "Procesador de 6 núcleos y 12 hilos", ComponentType.CPU,
        "Intel", BigDecimal.valueOf(153.97), 14);
    assertDoesNotThrow(() -> componentsService.save(component));

  }

  @Test
  void validateComponent() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Component component = new Component("Intel i5 12400F", "Procesador de 6 núcleos y 12 hilos", ComponentType.CPU,
        "Intel", BigDecimal.valueOf(153.97), 14);

    List<String> errors = componentsService.validateComponent(component, null);

    assertTrue(errors.isEmpty());

  }

  @Test
  void validateVoidComponent() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Component component = new Component("", "", ComponentType.CPU, "", BigDecimal.valueOf(153.97), 14);

    List<String> errors = componentsService.validateComponent(component, null);

    assertTrue(errors.contains("El nombre del componente es obligatorio."));
    assertTrue(errors.contains("La descripción es obligatoria."));
    assertTrue(errors.contains("La marca es obligatoria."));

  }

  @Test
  void validateNullComponent() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Component component = new Component(null, null, ComponentType.CPU, null, BigDecimal.valueOf(153.97), 14);

    List<String> errors = componentsService.validateComponent(component, null);

    assertTrue(errors.contains("El nombre del componente es obligatorio."));
    assertTrue(errors.contains("La descripción es obligatoria."));
    assertTrue(errors.contains("La marca es obligatoria."));

  }

  @Test
  void validateBadNumComponentData() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Component component = new Component("sampleComponent", "sampleDescription", ComponentType.CPU, "sampleBrand",
        BigDecimal.valueOf(-350.00), -5);

    List<String> errors = componentsService.validateComponent(component, null);

    assertTrue(errors.contains("El precio del producto ha de ser mayor de 0.00 €."));
    assertTrue(errors.contains("El stock del producto ha de ser 0 o mayor."));

  }

  @Test
  void validateExistingComponentData() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Component component = new Component("AMD Ryzen 7 7800X3D", "Procesador de 8 núcleos y 16 hilos", ComponentType.CPU,
        "AMD", BigDecimal.valueOf(350.00), 5);

    List<String> errors = componentsService.validateComponent(component, null);

    assertTrue(errors.contains("Ya existe un componente con ese nombre."));

  }

  @Test
  void postComponentImageAsNonRegisteredUser() throws IOException {

    Resource resource = new ClassPathResource("/sample_images/ryzen-7.webp");
    MockMultipartFile imageFile = new MockMultipartFile("imageFile", "ryzen-7.webp", "image/webp",
        resource.getInputStream());
    Component component = componentsRepository.findAll().get(0);

    assertThrows(AuthenticationCredentialsNotFoundException.class,
        () -> componentsService.addComponentImage(component.getId(), imageFile));

  }

  @Test
  void postComponentImageAsNoAdmin() throws IOException {
    User user = usersRepository.findByEmail("testuser1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"))));

    Resource expectedImage = new ClassPathResource("/sample_images/user.png");
    MockMultipartFile imageFile = new MockMultipartFile("imageFile", "user.png", "image/png",
        expectedImage.getInputStream());

    Component component = componentsRepository.findAll().get(0);
    assertThrows(AuthorizationDeniedException.class,
        () -> componentsService.addComponentImage(component.getId(), imageFile));

  }

  @Test
  void postImagesAsAdmin() throws IOException, SQLException {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Resource expectedImage = new ClassPathResource("/sample_images/ryzen-7.webp");
    MockMultipartFile imageFile = new MockMultipartFile("imageFile", "ryzen-7.webp", "image/webp",
        expectedImage.getInputStream());

    Component component = componentsRepository.findAll().get(0);
    Image savedComponentImage = componentsService.addComponentImage(component.getId(), imageFile);

    assertArrayEquals(expectedImage.getInputStream().readAllBytes(),
        savedComponentImage.getImageFile().getBinaryStream().readAllBytes());
  }

  @Test
  void postImageWithInvalidFile() {
    User user = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    MockMultipartFile invalidTypeImage = new MockMultipartFile(
        "image", "image.txt", "text/plain", new byte[1024]);
    Component component = componentsRepository.findAll().get(0);

    assertThrows(IllegalArgumentException.class,
        () -> componentsService.addComponentImage(component.getId(), invalidTypeImage));

  }

  @Test
  void deleteComponentAsNotLoggedIn() {

    assertThrows(AuthenticationCredentialsNotFoundException.class, () -> componentsService.delete(1L));

  }

  @Test
  void deleteComponentAsLoggedInNoAdmin() {
    User user = usersRepository.findByEmail("testuser1@example.com").orElseThrow();
    Component component = componentsRepository.findAll().get(0);

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER").build(),
            null, List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"))));

    assertThrows(AuthorizationDeniedException.class, () -> componentsService.delete(component.getId()));
    assertEquals(2, componentsRepository.count());

  }

  @Test
  void deleteComponentAsAdmin() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    Component component = componentsRepository.findAll().get(0);

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    assertDoesNotThrow(() -> componentsService.delete(component.getId()));
    assertEquals(1, componentsRepository.count());

  }

  @Test
  void updateImageAsNoRegisteredUser() throws IOException {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    Component component = componentsRepository.findAll().get(0);

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User
                .withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Resource originalResource = new ClassPathResource("/sample_images/rtx-5060.jpg");
    MockMultipartFile originalImageFile = new MockMultipartFile("imageFile", "rtx-5060.jpg", "image/jpeg",
        originalResource.getInputStream());
    componentsService.addComponentImage(component.getId(), originalImageFile);

    SecurityContextHolder.clearContext();

    Resource newResource = new ClassPathResource("/sample_images/rx-9060.jpg");
    MockMultipartFile newImageFile = new MockMultipartFile("imageFile", "rx-9060.jpg", "image/jpeg",
        newResource.getInputStream());

    assertThrows(AuthenticationCredentialsNotFoundException.class,
        () -> componentsService.addComponentImage(component.getId(), newImageFile));

  }

  @Test
  void updateImageAsRegisteredUserNoAdmin() throws IOException {
    User user = usersRepository.findByEmail("testuser1@example.com").orElseThrow();
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    Component component = componentsRepository.findAll().get(0);

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User
                .withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Resource originalResource = new ClassPathResource("/sample_images/rtx-5060.jpg");
    MockMultipartFile originalImageFile = new MockMultipartFile("imageFile", "rtx-5060.jpg", "image/jpeg",
        originalResource.getInputStream());
    componentsService.addComponentImage(component.getId(), originalImageFile);

    SecurityContextHolder.clearContext();

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User
                .withUsername(user.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"))));

    Resource newResource = new ClassPathResource("/sample_images/rx-9060.jpg");
    MockMultipartFile newImageFile = new MockMultipartFile("imageFile", "rx-9060.jpg", "image/jpeg",
        newResource.getInputStream());

    assertThrows(AuthorizationDeniedException.class,
        () -> componentsService.addComponentImage(component.getId(), newImageFile));

  }

  @Test
  void updateImagesAsAdmin() throws IOException, SQLException {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    Component component = componentsRepository.findAll().get(0);

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Resource originalResource = new ClassPathResource("/sample_images/rtx-5060.jpg");
    MockMultipartFile originalImageFile = new MockMultipartFile("imageFile", "rtx-5060.jpg", "image/jpeg",
        originalResource.getInputStream());
    Image originalImage = componentsService.addComponentImage(component.getId(), originalImageFile);

    Resource newResource = new ClassPathResource("/sample_images/rx-9060.jpg");
    MockMultipartFile newImageFile = new MockMultipartFile("imageFile", "rx-9060.jpg", "image/jpeg",
        newResource.getInputStream());

    Image newImage = componentsService.addComponentImage(component.getId(), newImageFile);

    assertNotEquals(originalImage.getImageFile().getBinaryStream().readAllBytes(),
        newImage.getImageFile().getBinaryStream().readAllBytes());

  }

  @Test
  void updateComponentAsNoRegisteredUser() {
    Component component = componentsRepository.findAll().get(0);

    Component updatedComponent = new Component("sampleName", "sampleDescription", ComponentType.CPU, "sampleBrand",
        BigDecimal.valueOf(100.00), 5);

    assertThrows(AuthenticationCredentialsNotFoundException.class,
        () -> componentsService.updateComponent(updatedComponent, component.getId()));
  }

  @Test
  void updateComponentAsRegisteredUserNoAdmin() {
    Component component = componentsRepository.findAll().get(0);

    Component updatedComponent = new Component("sampleName", "sampleDescription", ComponentType.CPU, "sampleBrand",
        BigDecimal.valueOf(100.00), 5);

    User user = usersRepository.findByEmail("testuser1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"))));

    assertThrows(AuthorizationDeniedException.class,
        () -> componentsService.updateComponent(updatedComponent, component.getId()));
  }

  @Test
  void updateComponentAsAdmin() {
    Component component = componentsRepository.findAll().get(0);

    Component updatedComponent = new Component("sampleName", "sampleDescription", ComponentType.CPU, "sampleBrand",
        BigDecimal.valueOf(100.00), 5);

    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Component savedComponent = componentsService.updateComponent(updatedComponent, component.getId());

    assertEquals("sampleName", savedComponent.getName());
    assertEquals("sampleDescription", savedComponent.getDescription());
    assertEquals(ComponentType.CPU, savedComponent.getType());
    assertEquals("sampleBrand", savedComponent.getBrand());
    assertEquals(BigDecimal.valueOf(100.00), savedComponent.getPrice());
    assertEquals(5, savedComponent.getStock());
  }

  @Test
  void updateVoidComponent() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    Component component = componentsRepository.findAll().get(0);

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Component updatedComponent = new Component("", "", ComponentType.CPU, "", BigDecimal.valueOf(100.00), 5);

    assertThrows(IllegalArgumentException.class,
        () -> componentsService.updateComponent(updatedComponent, component.getId()));
  }

  @Test
  void updateNullComponent() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    Component component = componentsRepository.findAll().get(0);

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Component updatedComponent = new Component(null, null, ComponentType.CPU, null, BigDecimal.valueOf(100.00), 5);

    assertThrows(IllegalArgumentException.class,
        () -> componentsService.updateComponent(updatedComponent, component.getId()));
  }

  @Test
  void updateExistingComponent() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    Component component = componentsRepository.findAll().get(0);

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Component updatedComponent = new Component("NVIDIA GeForce RTX 5060 WINDFORCE MAX OC", "sampleDescription",
        ComponentType.CPU, "sampleBrand", BigDecimal.valueOf(100.00), 5);

    assertThrows(IllegalArgumentException.class,
        () -> componentsService.updateComponent(updatedComponent, component.getId()));
  }

  @Test
  void updateBadFormatComponent() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    Component component = componentsRepository.findAll().get(0);

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Component updatedComponent = new Component("sampleName", "sampleDescription", ComponentType.CPU, "sampleBrand",
        BigDecimal.valueOf(-100.00), -5);

    assertThrows(IllegalArgumentException.class,
        () -> componentsService.updateComponent(updatedComponent, component.getId()));
  }

  @Test
  void updateValidation() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    Component originalComponent = componentsRepository.findAll().get(0);
    Component updatedComponent = new Component("sampleName", "sampleDescription", ComponentType.CPU, "sampleBrand",
        BigDecimal.valueOf(100.00), 5);

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    List<String> errors = componentsService.validateComponent(updatedComponent, originalComponent.getId());

    assertTrue(errors.isEmpty());
  }

  @Test
  void updateExistingValidationComponent() {
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    Component originalComponent = componentsRepository.findAll().get(0);
    Component updatedComponent = new Component("NVIDIA GeForce RTX 5060 WINDFORCE MAX OC", "sampleDescription",
        ComponentType.CPU, "sampleBrand", BigDecimal.valueOf(100.00), 5);

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    List<String> errors = componentsService.validateComponent(updatedComponent, originalComponent.getId());

    assertTrue(errors.contains("Ya existe un componente con ese nombre."));
  }

}