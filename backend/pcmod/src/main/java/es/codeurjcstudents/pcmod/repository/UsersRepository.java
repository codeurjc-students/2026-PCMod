package es.codeurjcstudents.pcmod.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.codeurjcstudents.pcmod.model.Image;
import es.codeurjcstudents.pcmod.model.User;

public interface UsersRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  @Query("SELECT u.image from UserTable u WHERE u.id = :id")
  Optional<Image> findImageById(long id);
}
