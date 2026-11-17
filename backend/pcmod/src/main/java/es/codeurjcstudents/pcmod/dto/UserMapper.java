package es.codeurjcstudents.pcmod.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import es.codeurjcstudents.pcmod.model.User;

@Mapper(componentModel = "spring", uses = { CartMapper.class, OrderMapper.class })
public interface UserMapper {

  UserDTO toDTO(User user);

  UserPublicDTO toPublicDTO(User user);

  @Mapping(target = "encodedPassword", ignore = true)
  @Mapping(target = "reviews", ignore = true)
  @Mapping(target = "cart", ignore = true)
  @Mapping(target = "orders", ignore = true)
  User toDomainFromFullDTO(UserFullDTO userFullDTO);

}