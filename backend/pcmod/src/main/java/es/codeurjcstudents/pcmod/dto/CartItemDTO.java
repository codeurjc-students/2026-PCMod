package es.codeurjcstudents.pcmod.dto;

public record CartItemDTO(
    Long id,
    ComponentBasicDTO component,
    int quantity
) {}