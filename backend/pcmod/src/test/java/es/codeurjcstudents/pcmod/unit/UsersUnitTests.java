package es.codeurjcstudents.pcmod.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import es.codeurjcstudents.pcmod.model.User;
import es.codeurjcstudents.pcmod.repository.UsersRepository;
import es.codeurjcstudents.pcmod.service.UsersService;

@Tag("server-unit")
public class UsersUnitTests {

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Test
  public void testCreateUser() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        passwordEncoder.encode("pass"), "REGISTERED_USER");

    when(usersRepository.save(user)).thenReturn(user);

    User actualUser = usersService.createUser(user);

    assertEquals(user, actualUser);
    verify(usersRepository).save(user);

  }

  @Test
  public void testGetUser() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        passwordEncoder.encode("pass"), "REGISTERED_USER");

    when(usersRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

    User actualUser = usersService.getUser(1L).orElseThrow();

    assertEquals(user, actualUser);
    verify(usersRepository).findById(1L);

  }

  @Test
  public void testGetUserException() {

    UsersRepository usersRepository = mock(UsersRepository.class);
    UsersService usersService = new UsersService(usersRepository);

    when(usersRepository.findById(1L)).thenReturn(java.util.Optional.empty());

    assertThrows(NoSuchElementException.class, () -> {
      usersService.getUser(1L).orElseThrow();
    });

  }

}
