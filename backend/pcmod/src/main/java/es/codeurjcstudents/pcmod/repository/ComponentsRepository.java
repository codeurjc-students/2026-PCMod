package es.codeurjcstudents.pcmod.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.codeurjcstudents.pcmod.model.Component;

public interface ComponentsRepository extends JpaRepository<Component, Long> {

  List<Component> findTop3ByOrderByIdDesc();

}