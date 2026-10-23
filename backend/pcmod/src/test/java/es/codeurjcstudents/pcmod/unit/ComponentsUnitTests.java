package es.codeurjcstudents.pcmod.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.io.IOException;

import javax.sql.rowset.serial.SerialBlob;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import es.codeurjcstudents.pcmod.model.Component;
import es.codeurjcstudents.pcmod.model.Image;
import es.codeurjcstudents.pcmod.repository.ComponentsRepository;
import es.codeurjcstudents.pcmod.service.ComponentsService;

@Tag("server-unit")
public class ComponentsUnitTests {

  @Test
  public void testComponentsLoad() {
    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository);
    Pageable pageable = mock(Pageable.class);

    List<Component> componentList = Arrays.asList(new Component(), new Component());
    Page<Component> expectedPage = new PageImpl<>(componentList);
    when(componentsRepository.findAll(pageable)).thenReturn(expectedPage);

    Page<Component> actualPage = componentsService.findAll(pageable);

    assertEquals(expectedPage, actualPage);
    verify(componentsRepository).findAll(pageable);
  }

  @Test
  public void testGetComponentImage() throws SQLException, IOException {
    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository);

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
    ComponentsService componentsService = new ComponentsService(componentsRepository);

    when(componentsRepository.findImageById(1L)).thenReturn(Optional.empty());

    Resource actualImage = componentsService.getImageFile(1L);

    assertEquals(null, actualImage);
    verify(componentsRepository).findImageById(1L);
  }

}
