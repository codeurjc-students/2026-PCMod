package es.codeurjcstudents.pcmod.dto;

import java.util.Collection;
import java.util.List;

import org.mapstruct.Mapper;

import es.codeurjcstudents.pcmod.model.Component;

@Mapper(componentModel = "spring", uses = {ReviewMapper.class})
public interface ComponentMapper {

  ComponentDTO toDTO(Component component);

  ComponentBasicDTO toBasicDTO(Component component);

  List<ComponentDTO> toDTOs(Collection<Component> components);

  List<ComponentBasicDTO> toBasicDTOs(Collection<Component> components);

  Component toDomain(ComponentDTO componentDTO);
}
