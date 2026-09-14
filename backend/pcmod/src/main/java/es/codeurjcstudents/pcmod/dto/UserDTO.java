package es.codeurjcstudents.pcmod.dto;

import java.util.List;

public record UserDTO(
    Long id,
    String name,
    String surname,
    String username,
    String address,
    String email,
    List<String> roles) {
}