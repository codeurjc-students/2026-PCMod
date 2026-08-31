package es.codeurjcstudents.pcmod.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import es.codeurjcstudents.pcmod.enums.ComponentType;
import es.codeurjcstudents.pcmod.model.Component;
import es.codeurjcstudents.pcmod.repository.ComponentsRepository;
import es.codeurjcstudents.pcmod.service.ComponentsService;

@Tag("server-integration")
@SpringBootTest
@Testcontainers
public class HomePageIntegrationTests {

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
    componentsRepository.save(new Component("Kingston NV3",
        "Capacidad: 1 TB\r\n" + //
            "Factor de forma de disco SSD: M.2\r\n" + //
            "Interfaz: PCI Express 4.0\r\n" + //
            "NVMe: Si\r\n" + //
            "Tipo de memoria: 3D NAND",
        ComponentType.STORAGE, "Kingston", BigDecimal.valueOf(149.95), 15));
  }

  @Test
  void loadRecentComponents() {

    List<Component> componentList = componentsService.findTop3MoreRecent();

    assertEquals(3, componentList.size());
    assertEquals("Kingston NV3", componentList.get(0).getName());
    assertEquals("NVIDIA GeForce RTX 5060 WINDFORCE MAX OC", componentList.get(1).getName());
    assertEquals("AMD Ryzen 7 7800X3D", componentList.get(2).getName());
  }

  @Test
  void loadWithoutRecentComponents() {

    componentsRepository.deleteAll();

    List<Component> componentList = componentsService.findTop3MoreRecent();

    assertTrue(componentList.isEmpty());
  }
}