package es.codeurjcstudents.pcmod.dto;

import java.math.BigDecimal;

public record ComponentDTO(
    Long id,
    String name,
    String description,
    String type,
    String brand,
    BigDecimal price,
    Integer stock) {
}
