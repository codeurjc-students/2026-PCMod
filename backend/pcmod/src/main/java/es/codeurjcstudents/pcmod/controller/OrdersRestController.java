package es.codeurjcstudents.pcmod.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjcstudents.pcmod.dto.OrderDTO;
import es.codeurjcstudents.pcmod.model.Order;
import es.codeurjcstudents.pcmod.model.User;
import es.codeurjcstudents.pcmod.service.OrdersService;
import es.codeurjcstudents.pcmod.service.UsersService;

@RestController
@RequestMapping("/api/v1/orders")
public class OrdersRestController {  
    
  @Autowired
  private OrdersService ordersService;

  @Autowired
  private UsersService usersService;

  @GetMapping("/")
  public Page<OrderDTO> getOrdersByUser(Pageable pageable, Principal principal) {
    User user = usersService.getUser(Long.valueOf(principal.getName()));
    return ordersService.getOrdersByUser(user, pageable).map(order -> ordersService.toDTO(order));
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
    Order order = ordersService.getOrder(id);
    return ResponseEntity.ok(ordersService.toDTO(order));
  }
}
