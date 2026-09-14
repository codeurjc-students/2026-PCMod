package es.codeurjcstudents.pcmod.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.codeurjcstudents.pcmod.model.User;

public interface UsersRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

}
