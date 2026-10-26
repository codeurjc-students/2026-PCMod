package es.codeurjcstudents.pcmod.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import es.codeurjcstudents.pcmod.dto.ComponentMapper;
import es.codeurjcstudents.pcmod.enums.ComponentType;
import es.codeurjcstudents.pcmod.model.Component;
import es.codeurjcstudents.pcmod.model.Image;
import es.codeurjcstudents.pcmod.repository.ComponentsRepository;
import es.codeurjcstudents.pcmod.service.ComponentsService;

@Tag("server-unit")
public class ComponentsUnitTests {

  @Test
  public void testComponentsLoad() {
    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentMapper componentMapper = mock(ComponentMapper.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper);
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
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper);

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
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper);

    when(componentsRepository.findById(12L)).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> componentsService.getComponent(12L));
    verify(componentsRepository).findById(12L);
  }

  @Test
  public void testGetComponentImage() throws SQLException, IOException {
    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentMapper componentMapper = mock(ComponentMapper.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper);

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
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper);

    when(componentsRepository.findImageById(1L)).thenReturn(Optional.empty());

    Resource actualImage = componentsService.getImageFile(1L);

    assertEquals(null, actualImage);
    verify(componentsRepository).findImageById(1L);
  }

  @Test
  public void testDeleteComponent() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentMapper componentMapper = mock(ComponentMapper.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper);

    Component component = new Component("Intel i5 12400F", "Procesador de 6 núcleos y 12 hilos", ComponentType.CPU, "Intel", BigDecimal.valueOf(153.97), 14);

    when(componentsRepository.findById(1L)).thenReturn(Optional.of(component));

    componentsService.delete(1L);

    verify(componentsRepository).deleteById(1L);

  }

  @Test
  public void testDeleteNonExistentComponent() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentMapper componentMapper = mock(ComponentMapper.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository, componentMapper);

    when(componentsRepository.findById(1L)).thenReturn(Optional.empty());

    componentsService.delete(1L);

    verify(componentsRepository,  never()).deleteById(1L);

  }

}
