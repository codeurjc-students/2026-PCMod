package es.codeurjcstudents.pcmod.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import es.codeurjcstudents.pcmod.model.Image;
import es.codeurjcstudents.pcmod.repository.ImageRepository;
import es.codeurjcstudents.pcmod.service.ImageService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.rowset.serial.SerialBlob;

@Tag("server-unit")
public class ImagesUnitTests {

  @Test
  public void testCreateImage() throws SQLException, IOException {

    ImageRepository imageRepository = mock(ImageRepository.class);
    ImageService imageService = new ImageService(imageRepository);

    String classpathResource = "/sample_images/i5-12400f.webp";
    Resource imagePath = new ClassPathResource(classpathResource);
    Image actualImage = imageService.createImage(imagePath.getInputStream());

    Image expectedImage = new Image();
    expectedImage.setImageFile(new SerialBlob(imagePath.getInputStream().readAllBytes()));

    assertEquals(expectedImage.getImageFile(), actualImage.getImageFile());
    verify(imageRepository).save(actualImage);

  }

  @Test
  public void testReplaceImage() throws SQLException, IOException {

    ImageRepository imageRepository = mock(ImageRepository.class);
    ImageService imageService = new ImageService(imageRepository);

    byte[] originalBytes = new byte[] { 1, 2, 3 };
    Image existingImage = new Image();
    existingImage.setImageFile(new SerialBlob(originalBytes));
    when(imageRepository.findById(1L)).thenReturn(Optional.of(existingImage));

    String classpathResource = "/sample_images/i5-12400f.webp";
    Resource imagePath = new ClassPathResource(classpathResource);
    Image updatedImage = imageService.replaceImageFile(1L, imagePath.getInputStream());
    byte[] updatedBytes = updatedImage.getImageFile().getBytes(1, (int) updatedImage.getImageFile().length());
    byte[] expectedBytes = imagePath.getInputStream().readAllBytes();

    assertEquals(expectedBytes.length, updatedBytes.length);
    assertNotEquals(originalBytes, updatedBytes);
    verify(imageRepository).save(updatedImage);

  }

  @Test
  public void testReplaceNoExistingImage() {

    ImageRepository imageRepository = mock(ImageRepository.class);
    ImageService imageService = new ImageService(imageRepository);

    when(imageRepository.findById(99L)).thenReturn(Optional.empty());

    String classpathResource = "/sample_images/i5-12400f.webp";
    Resource imagePath = new ClassPathResource(classpathResource);
    assertThrows(ResponseStatusException.class, () -> imageService.replaceImageFile(99L, imagePath.getInputStream()));

  }

  @Test
  public void testDeleteImage() {

    ImageRepository imageRepository = mock(ImageRepository.class);
    ImageService imageService = new ImageService(imageRepository);

    Image imageToDelete = new Image();
    when(imageRepository.findById(1L)).thenReturn(Optional.of(imageToDelete));

    imageService.deleteImage(1L);

    verify(imageRepository).deleteById(1L);

  }

  @Test
  public void testDeleteNoExistingImage() {

    ImageRepository imageRepository = mock(ImageRepository.class);
    ImageService imageService = new ImageService(imageRepository);

    when(imageRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> imageService.deleteImage(99L));

  }

  @Test
  void testValidateImage() {

    ImageRepository imageRepository = mock(ImageRepository.class);
    ImageService imageService = new ImageService(imageRepository);

    MockMultipartFile validImage = new MockMultipartFile(
        "image", "image.webp", "image/webp", new byte[1024]);
    MockMultipartFile invalidTypeImage = new MockMultipartFile(
        "image", "image.txt", "text/plain", new byte[1024]);
    MockMultipartFile imageWithoutType = new MockMultipartFile(
        "image", "image", null, new byte[1024]);
    MockMultipartFile oversizedImage = new MockMultipartFile(
        "image", "image.webp", "image/webp", new byte[10 * 1024 * 1024 + 1]);

    assertDoesNotThrow(() -> imageService.validate(validImage));
    assertThrows(ResponseStatusException.class, () -> imageService.validate(invalidTypeImage));
    assertThrows(ResponseStatusException.class, () -> imageService.validate(imageWithoutType));
    assertThrows(ResponseStatusException.class, () -> imageService.validate(oversizedImage));

  }

  @Test
  public void testFindAllImages() {
    ImageRepository imageRepository = mock(ImageRepository.class);
    ImageService imageService = new ImageService(imageRepository);

    imageService.findAll();

    verify(imageRepository).findAll();
  }

}
