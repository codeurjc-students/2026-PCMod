package es.codeurjcstudents.pcmod.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.codeurjcstudents.pcmod.model.Component;
import es.codeurjcstudents.pcmod.model.Image;

public interface ComponentsRepository extends JpaRepository<Component, Long> {

  List<Component> findTop3ByOrderByIdDesc();

  @Query("SELECT c.image from Component c WHERE c.id = :id")
  Optional<Image> findImageById(long id);

}