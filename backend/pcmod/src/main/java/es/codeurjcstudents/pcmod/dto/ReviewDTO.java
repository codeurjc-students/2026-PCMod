package es.codeurjcstudents.pcmod.dto;

public record ReviewDTO(
    Long id,
    String title,
    String message,
    Integer rating,
    String date,
    Long componentId,
    Long userId) {
}
