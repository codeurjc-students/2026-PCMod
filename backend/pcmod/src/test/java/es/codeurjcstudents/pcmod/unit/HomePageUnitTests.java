package es.codeurjcstudents.pcmod.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import es.codeurjcstudents.pcmod.enums.ComponentType;
import es.codeurjcstudents.pcmod.model.Component;
import es.codeurjcstudents.pcmod.repository.ComponentsRepository;
import es.codeurjcstudents.pcmod.service.ComponentsService;

@Tag("server-unit")
public class HomePageUnitTests {

  @Test
  public void testRecentComponentsLoad() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    ComponentsService componentsService = new ComponentsService(componentsRepository);

    Component component1 = new Component("AMD Ryzen 7 7800X3D",
        "8 núcleos y 16 hilos ideales para gaming\r\n" + //
            "96 MB de caché L3 3D V-Cache, baja latencia\r\n" + //
            "Compatible con memorias DDR5, doble canal\r\n" + //
            "Frecuencia base 4.2 GHz y turbo hasta 5.0 GHz",
        ComponentType.CPU, "AMD", BigDecimal.valueOf(350.00), 5);

    Component component2 = new Component("NVIDIA GeForce RTX 5060 WINDFORCE MAX OC",
        "Ray Tracing y DLSS 4 para gaming fluido\r\n" + //
            "8GB GDDR7 y arquitectura Blackwell\r\n" + //
            "WINDFORCE 2X: refrigeración eficiente y silenciosa\r\n" + //
            "DisplayPort 2.1 y HDMI 2.1b hasta 8K\r\n" + //
            "Tensor Cores 5ª gen y Reflex 2 para IA avanzada",
        ComponentType.GPU, "Gigabyte", BigDecimal.valueOf(359.85), 2);

    Component component3 = new Component("Kingston NV3",
        "Capacidad: 1 TB\r\n" + //
            "Factor de forma de disco SSD: M.2\r\n" + //
            "Interfaz: PCI Express 4.0\r\n" + //
            "NVMe: Si\r\n" + //
            "Tipo de memoria: 3D NAND",
        ComponentType.STORAGE, "Kingston", BigDecimal.valueOf(149.95), 15);

    List<Component> componentList = Arrays.asList(component1, component2, component3);
    when(componentsRepository.findTop3ByOrderByIdDesc()).thenReturn(componentList);

    List<Component> actualRecentComponents = componentsService.findTop3MoreRecent();

    assertEquals(componentList, actualRecentComponents);
    verify(componentsRepository).findTop3ByOrderByIdDesc();

  }

}
