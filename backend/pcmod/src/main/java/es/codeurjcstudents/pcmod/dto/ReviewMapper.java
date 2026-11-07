package es.codeurjcstudents.pcmod.dto;

import java.util.Collection;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import es.codeurjcstudents.pcmod.model.Review;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ReviewMapper {

  @Mapping(target = "componentId", source = "component.id")
  ReviewDTO toDTO(Review review);

  List<ReviewDTO> toDTOs(Collection<Review> reviews);

  @Mapping(target = "component", ignore = true)
  @Mapping(target = "user", ignore = true)
  Review toDomain(ReviewDTO reviewDTO);
}
