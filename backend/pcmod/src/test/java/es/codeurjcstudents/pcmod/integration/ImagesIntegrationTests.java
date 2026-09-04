package es.codeurjcstudents.pcmod.integration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.io.Resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import es.codeurjcstudents.pcmod.model.Image;
import es.codeurjcstudents.pcmod.repository.ImageRepository;
import es.codeurjcstudents.pcmod.service.ImageService;

@Tag("server-integration")
@SpringBootTest
@Testcontainers
public class ImagesIntegrationTests {

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
  private ImageService imageService;

  @Autowired
  private ImageRepository imageRepository;

  @BeforeEach
  void setUp() {
    imageRepository.deleteAll();
    imageRepository.save(new Image());
    imageRepository.save(new Image());
    imageRepository.save(new Image());
  }

  @Test
  void createImages() throws IOException {

    imageRepository.deleteAll();

    List<Image> imageList = imageService.findAll();
    assertEquals(0, imageList.size());

    Resource imagePath1 = new ClassPathResource("/sample_images/i5-12400f.webp");
    Image image1 = imageService.createImage(imagePath1.getInputStream());

    Resource imagePath2 = new ClassPathResource("/sample_images/kingston-nv3.webp");
    Image image2 = imageService.createImage(imagePath2.getInputStream());

    imageList = imageService.findAll();
    assertEquals(2, imageList.size());

    List<Long> idList = imageList.stream().map(Image::getId).toList();
    assertTrue(idList.contains(image1.getId()));
    assertTrue(idList.contains(image2.getId()));

  }

  @Test
  void replaceImage() throws IOException, SQLException {

    imageRepository.deleteAll();

    Resource originalPath = new ClassPathResource("/sample_images/kingston-nv3.webp");
    byte[] originalBytes = originalPath.getInputStream().readAllBytes();
    Image image = imageService.createImage(originalPath.getInputStream());

    Resource replacementPath = new ClassPathResource("/sample_images/i5-12400f.webp");
    byte[] expectedBytes = replacementPath.getInputStream().readAllBytes();

    Image updatedImage = imageService.replaceImageFile(image.getId(), replacementPath.getInputStream());
    byte[] updatedBytes = updatedImage.getImageFile().getBytes(1, (int) updatedImage.getImageFile().length());

    assertEquals(image.getId(), updatedImage.getId());
    assertFalse(Arrays.equals(originalBytes, updatedBytes));
    assertArrayEquals(expectedBytes, updatedBytes);

  }

  @Test
  void deleteImage() throws IOException {

    Long imageToDelete = imageRepository.findAll().getFirst().getId();
    imageService.deleteImage(imageToDelete);

    List<Image> imageList = imageService.findAll();
    assertEquals(2, imageList.size());

  }

}