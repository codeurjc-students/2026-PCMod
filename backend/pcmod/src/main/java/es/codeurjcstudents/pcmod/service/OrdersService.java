package es.codeurjcstudents.pcmod.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import es.codeurjcstudents.pcmod.dto.OrderDTO;
import es.codeurjcstudents.pcmod.dto.OrderMapper;
import es.codeurjcstudents.pcmod.model.Order;
import es.codeurjcstudents.pcmod.model.User;
import es.codeurjcstudents.pcmod.repository.OrdersRepository;

@Service
public class OrdersService {
  
  private final OrdersRepository ordersRepository;
  private final OrderMapper orderMapper;

  private static final String NOT_FOUND_MESSAGE = "Pedido no encontrado.";

  public OrdersService(OrdersRepository ordersRepository, OrderMapper orderMapper) {
    this.ordersRepository = ordersRepository;
    this.orderMapper = orderMapper;
  }

  @PreAuthorize("hasRole('ADMIN') or (#user.getId().toString() == principal.username)")
  public Page<Order> getOrdersByUser(User user, Pageable pageable) {
    return ordersRepository.findAllByUser(user, pageable);
  }
  
  @PreAuthorize("hasRole('ADMIN') or @ordersService.isOwner(#orderId, principal.username)")
  public Order getOrder(Long orderId) {
    return ordersRepository.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND_MESSAGE));
  }

  public boolean isOwner(long orderId, String usernameId) {

    Order order = ordersRepository.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND_MESSAGE));

    return order.getUser().getId().equals(Long.valueOf(usernameId));
  }

  public OrderDTO toDTO(Order order) {
    return orderMapper.toDTO(order);
  }

  public Order toDomain(OrderDTO orderDTO) {
    return orderMapper.toDomain(orderDTO);
  }
}
