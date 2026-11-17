package es.codeurjcstudents.pcmod.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import es.codeurjcstudents.pcmod.model.Cart;
import es.codeurjcstudents.pcmod.model.User;

public interface CartRepository extends JpaRepository<Cart, Long> {
  
  @EntityGraph(attributePaths = {"items"})
  Optional<Cart> findByUser(User user);

}