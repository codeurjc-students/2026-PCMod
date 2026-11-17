package es.codeurjcstudents.pcmod.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjcstudents.pcmod.dto.CartDTO;
import es.codeurjcstudents.pcmod.model.Cart;
import es.codeurjcstudents.pcmod.service.CartService;

@RestController
@RequestMapping("/api/v1/cart")
public class CartRestController {

  @Autowired
  private CartService cartService;

  @GetMapping("/")
  public ResponseEntity<CartDTO> getCart(Principal principal) {

    Cart cart = cartService.getCart(principal);

    return ResponseEntity.ok(cartService.toDTO(cart));
  }

}
