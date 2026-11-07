package es.codeurjcstudents.pcmod.dto;

import java.math.BigDecimal;
import java.util.List;

public record ComponentDTO(
    Long id,
    String name,
    String description,
    String type,
    String brand,
    BigDecimal price,
    Integer stock,
    List<ReviewDTO> reviews,
    Double averageRating,
    ImageDTO image) {
}
