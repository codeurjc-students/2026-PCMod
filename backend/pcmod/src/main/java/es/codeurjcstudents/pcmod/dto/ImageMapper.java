package es.codeurjcstudents.pcmod.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import es.codeurjcstudents.pcmod.model.Image;

@Mapper(componentModel = "spring")
public interface ImageMapper {

  ImageDTO toDTO(Image image);

  @Mapping(target = "imageFile", ignore = true)
  Image toDomain(ImageDTO imageDTO);
}