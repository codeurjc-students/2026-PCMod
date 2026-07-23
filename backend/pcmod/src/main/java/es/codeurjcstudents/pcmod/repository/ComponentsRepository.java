package es.codeurjcstudents.pcmod.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.codeurjcstudents.pcmod.model.Component;

public interface ComponentsRepository extends JpaRepository<Component, Long> {

}