package es.codeurjcstudents.pcmod.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import es.codeurjcstudents.pcmod.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

  UserDTO toDTO(User user);

  @Mapping(target = "encodedPassword", ignore = true)
  User toDomainFromFullDTO(UserFullDTO userFullDTO);

}