package es.codeurjcstudents.pcmod.service;

import java.util.List;

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

}
