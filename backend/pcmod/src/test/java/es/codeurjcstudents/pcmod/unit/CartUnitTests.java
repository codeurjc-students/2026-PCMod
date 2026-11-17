package es.codeurjcstudents.pcmod.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.security.Principal;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import es.codeurjcstudents.pcmod.dto.CartMapper;
import es.codeurjcstudents.pcmod.model.Cart;
import es.codeurjcstudents.pcmod.model.User;
import es.codeurjcstudents.pcmod.repository.CartRepository;
import es.codeurjcstudents.pcmod.repository.UsersRepository;
import es.codeurjcstudents.pcmod.service.CartService;

@Tag("server-unit")
public class CartUnitTests {

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Test
  public void testCartLoadAsNoLoggedIn() {
    CartRepository cartRepository = mock(CartRepository.class);
    CartMapper cartMapper = mock(CartMapper.class);
    UsersRepository usersRepository = mock(UsersRepository.class);
    CartService cartService = new CartService(cartRepository, cartMapper, usersRepository);

    assertThrows(NullPointerException.class, () -> cartService.getCart(null));
    verify(cartRepository, never()).findByUser(any());
  }

  @Test
  public void testCartLoadWithBadUserId() {
    CartRepository cartRepository = mock(CartRepository.class);
    CartMapper cartMapper = mock(CartMapper.class);
    UsersRepository usersRepository = mock(UsersRepository.class);
    Principal principal = mock(Principal.class);
    CartService cartService = new CartService(cartRepository, cartMapper, usersRepository);

    when(principal.getName()).thenReturn("1");
    when(usersRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> cartService.getCart(principal));
    verify(cartRepository, never()).findByUser(any());
  }

  @Test 
  public void testCartLoadWithNoExistingCart() {
    CartRepository cartRepository = mock(CartRepository.class);
    CartMapper cartMapper = mock(CartMapper.class);
    UsersRepository usersRepository = mock(UsersRepository.class);
    Principal principal = mock(Principal.class);
    CartService cartService = new CartService(cartRepository, cartMapper, usersRepository);

   User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        passwordEncoder.encode("pass"), "REGISTERED_USER");

    when(principal.getName()).thenReturn("1");
    when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
    when(cartRepository.findByUser(any())).thenReturn(Optional.empty());

    cartService.getCart(principal);

    verify(cartRepository).findByUser(any());
    verify(cartRepository).save(any());
  }

  @Test 
  public void testCartLoadWithExistingCart() {
    CartRepository cartRepository = mock(CartRepository.class);
    CartMapper cartMapper = mock(CartMapper.class);
    UsersRepository usersRepository = mock(UsersRepository.class);
    Principal principal = mock(Principal.class);
    CartService cartService = new CartService(cartRepository, cartMapper, usersRepository);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        passwordEncoder.encode("pass"), "REGISTERED_USER");
    Cart userCart = new Cart(user);

    when(principal.getName()).thenReturn("1");
    when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
    when(cartRepository.findByUser(any())).thenReturn(Optional.of(userCart));

    Cart cart = cartService.getCart(principal);

    verify(cartRepository).findByUser(any());
    verify(cartRepository, never()).save(any());
    assertEquals(userCart, cart);
  }
  
}
