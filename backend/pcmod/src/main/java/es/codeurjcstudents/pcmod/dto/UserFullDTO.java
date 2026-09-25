package es.codeurjcstudents.pcmod.dto;

import java.util.List;

public record UserFullDTO(
    Long id,
    String name,
    String surname,
    String username,
    String address,
    String email,
    String password,
    ImageDTO image,
    List<String> roles) {
}