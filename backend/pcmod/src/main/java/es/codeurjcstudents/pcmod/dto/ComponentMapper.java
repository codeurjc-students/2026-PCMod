package es.codeurjcstudents.pcmod.dto;

import java.util.Collection;
import java.util.List;

import org.mapstruct.Mapper;

import es.codeurjcstudents.pcmod.model.Component;

@Mapper(componentModel = "spring")
public interface ComponentMapper {

  ComponentDTO toDTO(Component component);

  List<ComponentDTO> toDTOs(Collection<Component> components);

  Component toDomain(ComponentDTO componentDTO);
}
