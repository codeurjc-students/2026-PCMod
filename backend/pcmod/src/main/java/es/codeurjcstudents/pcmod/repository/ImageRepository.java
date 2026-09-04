package es.codeurjcstudents.pcmod.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.codeurjcstudents.pcmod.model.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {

}