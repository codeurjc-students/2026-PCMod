package es.codeurjcstudents.pcmod.dto;

import java.util.List;

public record CartDTO(
    Long id,
    List<CartItemDTO> items
) {}