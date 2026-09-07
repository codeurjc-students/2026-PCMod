package es.codeurjcstudents.pcmod.dto;

import org.mapstruct.Mapper;

import es.codeurjcstudents.pcmod.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

  UserDTO toDTO(User user);

}