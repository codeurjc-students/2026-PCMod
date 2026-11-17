package es.codeurjcstudents.pcmod.service;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import es.codeurjcstudents.pcmod.dto.CartDTO;
import es.codeurjcstudents.pcmod.dto.CartMapper;
import es.codeurjcstudents.pcmod.model.Cart;
import es.codeurjcstudents.pcmod.model.User;
import es.codeurjcstudents.pcmod.repository.CartRepository;
import es.codeurjcstudents.pcmod.repository.UsersRepository;

@Service 
public class CartService {

  private final CartRepository cartRepository;
  private final CartMapper cartMapper;
  private final UsersRepository usersRepository;

  private static final String USER_NOT_FOUND_MESSAGE = "Usuario no encontrado.";

  public CartService(CartRepository cartRepository, CartMapper cartMapper, UsersRepository usersRepository) {
    this.cartRepository = cartRepository;
    this.cartMapper = cartMapper;
    this.usersRepository = usersRepository;
  }
  
  @Transactional
  public Cart getCart(Principal principal) {
    
    User user = usersRepository.findById(Long.valueOf(principal.getName())).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_MESSAGE));
    return cartRepository.findByUser(user).orElseGet(() -> {
      Cart newCart = new Cart(user);
      return cartRepository.save(newCart);
    });
    
  }

  public CartDTO toDTO(Cart cart) {
    return cartMapper.toDTO(cart);
  }

  public Cart toDomain(CartDTO cartDTO) {
    return cartMapper.toDomain(cartDTO);
  }

}
