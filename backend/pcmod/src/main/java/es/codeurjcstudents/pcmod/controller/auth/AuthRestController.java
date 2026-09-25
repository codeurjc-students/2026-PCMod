package es.codeurjcstudents.pcmod.controller.auth;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath;

import es.codeurjcstudents.pcmod.dto.UserDTO;
import es.codeurjcstudents.pcmod.dto.UserFullDTO;
import es.codeurjcstudents.pcmod.model.User;
import es.codeurjcstudents.pcmod.security.jwt.AuthResponse;
import es.codeurjcstudents.pcmod.security.jwt.AuthResponse.Status;
import es.codeurjcstudents.pcmod.service.UsersService;
import es.codeurjcstudents.pcmod.security.jwt.LoginRequest;
import es.codeurjcstudents.pcmod.security.jwt.UserLoginService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthRestController {

  @Autowired
  private UserLoginService userLoginService;

  @Autowired
  private UsersService userService;

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
      @RequestBody LoginRequest loginRequest,
      HttpServletResponse response) {

    return userLoginService.login(response, loginRequest);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refreshToken(
      @CookieValue(name = "RefreshToken", required = false) String refreshToken, HttpServletResponse response) {

    return userLoginService.refresh(response, refreshToken);
  }

  @PostMapping("/logout")
  public ResponseEntity<AuthResponse> logOut(HttpServletResponse response) {
    return ResponseEntity.ok(new AuthResponse(Status.SUCCESS, userLoginService.logout(response)));
  }

  @PostMapping("/register")
  public ResponseEntity<UserDTO> register(@RequestBody UserFullDTO userFullDTO) {

    User user = userService.toDomainFromFullDTO(userFullDTO);
    User createdUser = userService.register(user, userFullDTO.password());

    URI location = fromCurrentContextPath().path("/api/v1/users/{id}")
        .buildAndExpand(createdUser.getId()).toUri();

    return ResponseEntity.created(location).body(userService.toDTO(createdUser));

  }

}