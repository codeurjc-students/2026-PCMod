package es.codeurjcstudents.pcmod.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.math.BigDecimal;

import javax.sql.rowset.serial.SerialBlob;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import es.codeurjcstudents.pcmod.dto.ComponentMapper;
import es.codeurjcstudents.pcmod.enums.ComponentType;
import es.codeurjcstudents.pcmod.model.Component;
import es.codeurjcstudents.pcmod.model.Image;
import es.codeurjcstudents.pcmod.repository.ComponentsRepository;
import es.codeurjcstudents.pcmod.service.ComponentsService;
import es.codeurjcstudents.pcmod.service.ImageService;

@Tag("server-unit")
public class ComponentsUnitTests {

  @Test
  public void testComponentsLoad() {
    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentMapper componentMapper = mock(ComponentMapper.class);
    ImageService imageService = mock(ImageService.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper, imageService);
    Pageable pageable = mock(Pageable.class);

    List<Component> componentList = Arrays.asList(new Component(), new Component());
    Page<Component> expectedPage = new PageImpl<>(componentList);
    when(componentsRepository.findAll(pageable)).thenReturn(expectedPage);

    Page<Component> actualPage = componentsService.findAll(pageable);

    assertEquals(expectedPage, actualPage);
    verify(componentsRepository).findAll(pageable);
  }

  @Test
  public void testGetComponent() {
    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentMapper componentMapper = mock(ComponentMapper.class);
    ImageService imageService = mock(ImageService.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper, imageService);

    Component componentToReturn = new Component("Intel i5 12400F", "Procesador de 6 núcleos y 12 hilos", ComponentType.CPU, "Intel", BigDecimal.valueOf(153.97), 14);

    when(componentsRepository.findById(1L)).thenReturn(Optional.of(componentToReturn));

    Component component = componentsService.getComponent(1L);

    assertEquals(componentToReturn, component);
    verify(componentsRepository).findById(1L);
  }

  @Test
  public void testGetComponentNotFound() {
    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentMapper componentMapper = mock(ComponentMapper.class);
    ImageService imageService = mock(ImageService.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper, imageService);

    when(componentsRepository.findById(12L)).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> componentsService.getComponent(12L));
    verify(componentsRepository).findById(12L);
  }

  @Test
  public void testGetComponentImage() throws SQLException, IOException {
    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentMapper componentMapper = mock(ComponentMapper.class);
    ImageService imageService = mock(ImageService.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper, imageService);

    byte[] originalBytes = new byte[] { 1, 2, 3 };
    Image existingImage = new Image();
    existingImage.setImageFile(new SerialBlob(originalBytes));
    when(componentsRepository.findImageById(1L)).thenReturn(Optional.of(existingImage));

    Resource actualImage = componentsService.getImageFile(1L);

    assertArrayEquals(originalBytes, actualImage.getInputStream().readAllBytes());
    verify(componentsRepository).findImageById(1L);
  }

  @Test
  public void testGetNullComponentImage() throws SQLException {
    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentMapper componentMapper = mock(ComponentMapper.class);
    ImageService imageService = mock(ImageService.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper, imageService);

    when(componentsRepository.findImageById(1L)).thenReturn(Optional.empty());

    Resource actualImage = componentsService.getImageFile(1L);

    assertEquals(null, actualImage);
    verify(componentsRepository).findImageById(1L);
  }

  @Test
  public void testCreateComponent() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentMapper componentMapper = mock(ComponentMapper.class);
    ImageService imageService = mock(ImageService.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper, imageService);

    Component component = new Component("Intel i5 12400F", "Procesador de 6 núcleos y 12 hilos", ComponentType.CPU, "Intel", BigDecimal.valueOf(153.97), 14);

    when(componentsRepository.save(any(Component.class))).thenReturn(component);

    Component savedComponent = componentsService.save(component);

    assertEquals(component.getName(), savedComponent.getName());
    assertEquals(component.getDescription(), savedComponent.getDescription());
    assertEquals(component.getType(), savedComponent.getType());
    assertEquals(component.getBrand(), savedComponent.getBrand());
    assertEquals(component.getPrice(), savedComponent.getPrice());
    assertEquals(component.getStock(), savedComponent.getStock());
    verify(componentsRepository).save(any(Component.class));

  }

  @Test
  public void testCreateVoidComponentData() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentMapper componentMapper = mock(ComponentMapper.class);
    ImageService imageService = mock(ImageService.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper, imageService);

    Component component = new Component("", "", ComponentType.CPU, "", BigDecimal.valueOf(153.97), 14);

    assertThrows(IllegalArgumentException.class, () -> componentsService.save(component));
    verify(componentsRepository, never()).save(any(Component.class));

  }

  @Test
  public void testCreateNullComponentData() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentMapper componentMapper = mock(ComponentMapper.class);
    ImageService imageService = mock(ImageService.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper, imageService);

    Component component = new Component(null, null, ComponentType.CPU, null, BigDecimal.valueOf(153.97), 14);

    assertThrows(IllegalArgumentException.class, () -> componentsService.save(component));
    verify(componentsRepository, never()).save(any(Component.class));

  }

  @Test
  public void testCreateExistingComponentData() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentMapper componentMapper = mock(ComponentMapper.class);
    ImageService imageService = mock(ImageService.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper, imageService);

    Component component = new Component("Intel i5 12400F", "Procesador de 6 núcleos y 12 hilos", ComponentType.CPU, "Intel", BigDecimal.valueOf(153.97), 14);

    when(componentsRepository.existsByName("Intel i5 12400F")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> componentsService.save(component));
    verify(componentsRepository, never()).save(any(Component.class));

  }

  @Test
  public void testValidation() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentMapper componentMapper = mock(ComponentMapper.class);
    ImageService imageService = mock(ImageService.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper, imageService);

    Component component = new Component("Intel i5 12400F", "Procesador de 6 núcleos y 12 hilos", ComponentType.CPU, "Intel", BigDecimal.valueOf(153.97), 14);
    List<String> errors = componentsService.validateComponent(component);

    assertTrue(errors.isEmpty());

  }

  @Test
  public void testVoidComponentValidation() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentMapper componentMapper = mock(ComponentMapper.class);
    ImageService imageService = mock(ImageService.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper, imageService);

    Component component = new Component("", "", ComponentType.CPU, "", BigDecimal.valueOf(153.97), 14);
    List<String> errors = componentsService.validateComponent(component);

    assertTrue(errors.contains("El nombre del componente es obligatorio."));
    assertTrue(errors.contains("La descripción es obligatoria."));
    assertTrue(errors.contains("La marca es obligatoria."));

  }

  @Test
  public void testNullComponentValidation() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentMapper componentMapper = mock(ComponentMapper.class);
    ImageService imageService = mock(ImageService.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper, imageService);

    Component component = new Component(null, null, ComponentType.CPU, null, BigDecimal.valueOf(153.97), 14);
    List<String> errors = componentsService.validateComponent(component);

    assertTrue(errors.contains("El nombre del componente es obligatorio."));
    assertTrue(errors.contains("La descripción es obligatoria."));
    assertTrue(errors.contains("La marca es obligatoria."));

  }

  @Test
  public void testExistingComponentValidation() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentMapper componentMapper = mock(ComponentMapper.class);
    ImageService imageService = mock(ImageService.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper, imageService);

    Component component = new Component("Intel i5 12400F", "Procesado de 6 núcleos y 12 hilos", ComponentType.CPU, "Intel", BigDecimal.valueOf(153.97), 14);
    when(componentsRepository.existsByName("Intel i5 12400F")).thenReturn(true);

    List<String> errors = componentsService.validateComponent(component);

    assertTrue(errors.contains("Ya existe un componente con ese nombre."));

  }

  @Test
  public void testAddImageFile() throws Exception {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentMapper componentMapper = mock(ComponentMapper.class);
    ImageService imageService = mock(ImageService.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper, imageService);

    Component component = new Component("Intel i5 12400F", "Procesado de 6 núcleos y 12 hilos", ComponentType.CPU, "Intel", BigDecimal.valueOf(153.97), 14);
        
    Resource expectedImage = new ClassPathResource("/sample_images/i5-12400f.webp");
    MockMultipartFile imageFile = new MockMultipartFile(
        "imageFile", "user.png", "image/png", expectedImage.getInputStream());
    Image image = new Image(new SerialBlob(expectedImage.getInputStream().readAllBytes()));
    when(imageService.createImage(any())).thenReturn(image);

    when(componentsRepository.findById(1L)).thenReturn(Optional.of(component));
    when(componentsRepository.save(component)).thenReturn(component);

    Image actualImage = componentsService.addComponentImage(1L, imageFile);

    assertArrayEquals(expectedImage.getInputStream().readAllBytes(),
        actualImage.getImageFile().getBinaryStream().readAllBytes());

  }

  @Test
  public void testAddImageWithInvalidFile() throws IOException {
    
    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentMapper componentMapper = mock(ComponentMapper.class);
    ImageService imageService = mock(ImageService.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper, imageService);

    Component component = new Component("Intel i5 12400F", "Procesado de 6 núcleos y 12 hilos", ComponentType.CPU, "Intel", BigDecimal.valueOf(153.97), 14);
    when(componentsRepository.findById(1L)).thenReturn(Optional.of(component));

    MockMultipartFile invalidTypeImage = new MockMultipartFile(
        "image", "image.txt", "text/plain", new byte[1024]);

    when(imageService.createImage(any())).thenThrow(IllegalArgumentException.class);

    assertThrows(IllegalArgumentException.class, () -> componentsService.addComponentImage(1L, invalidTypeImage));
  }

  @Test
  public void testDeleteComponent() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentMapper componentMapper = mock(ComponentMapper.class);
    ImageService imageService = mock(ImageService.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper, imageService);

    Component component = new Component("Intel i5 12400F", "Procesador de 6 núcleos y 12 hilos", ComponentType.CPU, "Intel", BigDecimal.valueOf(153.97), 14);

    when(componentsRepository.findById(1L)).thenReturn(Optional.of(component));

    componentsService.delete(1L);

    verify(componentsRepository).deleteById(1L);

  }

  @Test
  public void testDeleteNonExistentComponent() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentMapper componentMapper = mock(ComponentMapper.class);
    ImageService imageService = mock(ImageService.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper, imageService);

    when(componentsRepository.findById(1L)).thenReturn(Optional.empty());

    componentsService.delete(1L);

    verify(componentsRepository,  never()).deleteById(1L);

  }

}
