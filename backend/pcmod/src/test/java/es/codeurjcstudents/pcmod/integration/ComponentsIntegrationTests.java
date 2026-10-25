package es.codeurjcstudents.pcmod.integration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import es.codeurjcstudents.pcmod.enums.ComponentType;
import es.codeurjcstudents.pcmod.model.Component;
import es.codeurjcstudents.pcmod.repository.ComponentsRepository;
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
    Component component = componentsRepository.save(new Component("Intel i5 12400F", "Procesador de 6 núcleos y 12 hilos", ComponentType.CPU, "Intel", BigDecimal.valueOf(153.97), 14));

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

}