package es.codeurjcstudents.pcmod.controller;

import java.security.Principal;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjcstudents.pcmod.dto.UserDTO;
import es.codeurjcstudents.pcmod.dto.UserMapper;
import es.codeurjcstudents.pcmod.model.User;
import es.codeurjcstudents.pcmod.service.UsersService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/users")
public class UsersRestController {

  @Autowired
  UsersService usersService;

  @Autowired
  UserMapper userMapper;

  @GetMapping("/me")
  public UserDTO me(HttpServletRequest request) {

    Principal principal = request.getUserPrincipal();

    if (principal != null) {

      User user = usersService.getUser(Long.parseLong(principal.getName())).orElseThrow();
      return userMapper.toDTO(user);

    } else {

      throw new NoSuchElementException();

    }
  }
}
