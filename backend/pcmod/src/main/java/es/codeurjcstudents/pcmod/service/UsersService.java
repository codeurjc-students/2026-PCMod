package es.codeurjcstudents.pcmod.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import es.codeurjcstudents.pcmod.model.User;
import es.codeurjcstudents.pcmod.repository.UsersRepository;

@Service
public class UsersService {

  private final UsersRepository userRepository;

  public UsersService(UsersRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User createUser(User user) {
    return userRepository.save(user);
  }

  public Optional<User> getUser(long id) {
    return userRepository.findById(id);
  }

}
