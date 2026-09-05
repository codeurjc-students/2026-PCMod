package es.codeurjcstudents.pcmod.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjcstudents.pcmod.dto.ComponentMapper;
import es.codeurjcstudents.pcmod.model.Image;
import es.codeurjcstudents.pcmod.repository.ImageRepository;

@Service
public class ImageService {

  private static final long MAX_SIZE = 10L * 1024L * 1024L; // 10MB
  private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp");

  private final ImageRepository imageRepository;

  @Autowired
  private ComponentMapper componentMapper;

  public ImageService(ImageRepository imageRepository) {
    this.imageRepository = imageRepository;
  }

  public List<Image> findAll() {
    return imageRepository.findAll();
  }

  public Image createImage(InputStream inputStream) throws IOException {

    Image image = new Image();

    try {
      image.setImageFile(new SerialBlob(inputStream.readAllBytes()));
    } catch (Exception e) {
      throw new IOException("Failed to create image", e);
    }

    imageRepository.save(image);

    return image;
  }

  public Image replaceImageFile(long id, InputStream inputStream) throws IOException {

    Image image = imageRepository.findById(id).orElseThrow();

    try {
      image.setImageFile(new SerialBlob(inputStream.readAllBytes()));
    } catch (Exception e) {
      throw new IOException("Failed to create image", e);
    }

    imageRepository.save(image);

    return image;
  }

  public Image deleteImage(long id) {

    Image image = imageRepository.findById(id).orElseThrow();
    imageRepository.deleteById(id);

    return image;
  }

  public void validate(MultipartFile imageField) {

    if (imageField.getSize() > MAX_SIZE) {
      throw new IllegalArgumentException("El tamaño de la imagen no puede superar los 10MB.");
    }

    String contentType = imageField.getContentType();
    if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
      throw new IllegalArgumentException(
          "El tipo de archivo no es válido. Solo se permiten imágenes JPEG, PNG y WebP.");
    }

  }
}