package es.codeurjcstudents.pcmod.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjcstudents.pcmod.enums.ComponentType;
import es.codeurjcstudents.pcmod.model.Component;
import es.codeurjcstudents.pcmod.repository.ComponentsRepository;
import jakarta.annotation.PostConstruct;

@Service
public class DatabaseInitializer {

  @Autowired
  private ComponentsRepository componentsRepository;

  @PostConstruct
  public void init() {

    // Sample Components

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

    componentsRepository.save(new Component("Corsair Vengeance LPX 2X8GB",
        "Latencia CAS: 16\r\n" + //
            "Memoria interna: 16 GB\r\n" + //
            "Tipo de memoria interna: DDR4\r\n" + //
            "Velocidad de memoria del reloj: 3200 MHz",
        ComponentType.RAM, "Corsair", BigDecimal.valueOf(149.95), 2));

    componentsRepository.save(new Component("MAG A650BN",
        "Potencia total: 650 W\r\n" + //
            "Voltaje de entrada AC: 100 - 240 V\r\n" + //
            "Frecuencia de entrada AC: 50/60 Hz\r\n" + //
            "Corrección del factor de potencia tipo (PFC): Activo\r\n" + //
            "Potencia combinada (3,3 V): 110 W\r\n" + //
            "Potencia combinada (+5 V): 110 W\r\n" + //
            "Corriente máxima de salida (+3.3V): 20 A\r\n" + //
            "Corriente máxima de salida (+12V): 54 A\r\n" + //
            "Corriente máxima de salida (+5Vsb): 20 A\r\n" + //
            "Funciones de protección de poder: Sobreintensidad, Sobretensión, Sobrevoltaje, Cortocircuito",
        ComponentType.PSU, "MSI", BigDecimal.valueOf(55.90), 20));

    componentsRepository.save(new Component("Asus TUF GAMING B550M-PLUS WIFI II",
        "Fabricante de procesador: AMD\r\n" + //
            "Socket de procesador: Zócalo AM4\r\n" + //
            "Procesador compatible: AMD Ryzen 3, AMD Ryzen 5, AMD Ryzen 7, 3rd Generation AMD Ryzen 9, AMD Ryzen 9 5th Gen\r\n"
            + //
            "Sockets de procesador soportados: Zócalo AM4\r\n" + //
            "Tipos de memoria compatibles: DDR4-SDRAM\r\n" + //
            "Número de ranuras de memoria: 4\r\n" + //
            "Tipo de ranuras de memoria: DIMM\r\n" + //
            "Canales de memoria: Dual-channel",
        ComponentType.MOTHERBOARD, "Asus", BigDecimal.valueOf(118.90), 15));

    componentsRepository.save(new Component("Lian Li Vector V100",
        "Formato: ATX (Midi Tower), compatibilidad total con EATX, Micro ATX y Mini-ITX\r\n" + //
            "Materiales: Acero, Vidrio templado, Plástico. Acabado negro profesional y paneles transparentes\r\n" + //
            "Bahías de almacenamiento: 1 x 3.5\", 1 x 2.5\" (total 2 unidades soportadas)\r\n" + //
            "Expansión: 7 slots PCI\r\n" + //
            "Ventiladores incluidos: 3 laterales de 120 mm (ARGB), 1 trasero de 120 mm (ARGB)\r\n" + //
            "Refrigeración total: hasta 3 ventiladores laterales, 3 superiores (120/140 mm), 1 trasero (120/140 mm), soporte para radiadores de hasta 360 mm superior\r\n"
            + //
            "Iluminación: ARGB multi, completamente personalizable\r\n" + //
            "Conectividad frontal: 2 x USB 3.0, 1 x USB Type-C, combo audio/micrófono\r\n" + //
            "Peso aproximado: 14,38 kg (volumétrico)",
        ComponentType.CASE, "Lian Li", BigDecimal.valueOf(65.99), 5));

    componentsRepository.save(new Component("Intel Core i5-12400F",
        "6 núcleos de alto rendimiento con 12 hilos\r\n" + //
            "Frecuencias de 2.5 a 4.4GHz para gaming avanzado\r\n" + //
            "Compatible con memorias DDR4 y DDR5 hasta 128GB\r\n" + //
            "Incluye refrigerador en caja y arquitectura Alder Lake\r\n" + //
            "PCIe 5.0 y 4.0 para GPUs/SSDs de nueva generación\r\n" + //
            "No incluye gráficos integrados, necesita gráfica dedicada",
        ComponentType.CPU, "Intel",
        BigDecimal.valueOf(153.97), 14));

    componentsRepository.save(new Component("AMD Radeon RX 9060 XT DUAL WHITE",
        "16GB GDDR6 para gaming y creación profesional\r\n" + //
            "PCIe 5.0 y diseño blanco compacto\r\n" + //
            "Refrigeración dual ASUS Axial-tech silenciosa\r\n" + //
            "Soporta hasta 3 monitores simultáneos\r\n" + //
            "Frecuencias OC hasta 3 250 MHz",
        ComponentType.GPU, "Asus", BigDecimal.valueOf(482.90), 5));

    componentsRepository.save(new Component("Seagate BarraCuda 3.5",
        "Capacidad de 2 TB para grandes volúmenes de datos\r\n" + //
            "Velocidad de 7200 rpm para acceso rápido\r\n" + //
            "Interfaz SATA III de 6 Gb/s\r\n" + //
            "Buffer de 256 MB optimiza el rendimiento\r\n" + //
            "Compatible con PC de sobremesa y estaciones de trabajo\r\n" + //
            "Alta fiabilidad y durabilidad para uso intensivo",
        ComponentType.STORAGE, "Seagate", BigDecimal.valueOf(121.99), 1));

    componentsRepository.save(new Component("Kingston FURY Beast",
        "Latencia CAS: 36\r\n" + //
            "Memoria interna: 16 GB\r\n" + //
            "Diseño de memoria (módulos x tamaño): 2 x 8 GB\r\n" + //
            "Tipo de memoria interna: DDR5\r\n" + //
            "Velocidad de memoria del reloj: 6000 MHz",
        ComponentType.RAM, "Kingston", BigDecimal.valueOf(269.00), 15));

  }

}
