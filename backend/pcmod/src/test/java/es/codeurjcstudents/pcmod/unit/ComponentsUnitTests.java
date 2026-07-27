package es.codeurjcstudents.pcmod.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import es.codeurjcstudents.pcmod.model.Component;
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

}
