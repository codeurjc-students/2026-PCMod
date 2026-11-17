package es.codeurjcstudents.pcmod.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import es.codeurjcstudents.pcmod.dto.OrderMapper;
import es.codeurjcstudents.pcmod.enums.OrderStatus;
import es.codeurjcstudents.pcmod.model.Order;
import es.codeurjcstudents.pcmod.model.User;
import es.codeurjcstudents.pcmod.repository.OrdersRepository;
import es.codeurjcstudents.pcmod.service.OrdersService;

@Tag("server-unit")
public class OrdersUnitTests {

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Test
  public void testOrdersByUserLoad() {
    OrdersRepository ordersRepository = mock(OrdersRepository.class);
    OrderMapper orderMapper = mock(OrderMapper.class);
    OrdersService ordersService = new OrdersService(ordersRepository, orderMapper);
    Pageable pageable = mock(Pageable.class);

    List<Order> orderList = Arrays.asList(new Order(), new Order());
    Page<Order> expectedPage = new PageImpl<>(orderList);
    when(ordersRepository.findAllByUser(any(), eq(pageable))).thenReturn(expectedPage);

    Page<Order> actualPage = ordersService.getOrdersByUser(new User(), pageable);

    assertEquals(expectedPage, actualPage);
    verify(ordersRepository).findAllByUser(any(), eq(pageable));
  }

  @Test
  public void testInexistentOrderLoad() {
    OrdersRepository ordersRepository = mock(OrdersRepository.class);
    OrderMapper orderMapper = mock(OrderMapper.class);
    OrdersService ordersService = new OrdersService(ordersRepository, orderMapper);

    when(ordersRepository.findById(any())).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> ordersService.getOrder(1L));
    verify(ordersRepository).findById(any());
  }

  @Test
  public void testOrderLoad() {
    OrdersRepository ordersRepository = mock(OrdersRepository.class);
    OrderMapper orderMapper = mock(OrderMapper.class);
    OrdersService ordersService = new OrdersService(ordersRepository, orderMapper);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
    passwordEncoder.encode("pass"), "REGISTERED_USER");

    Order order = new Order(new User(), OrderStatus.FINISHED, "example_address", LocalDateTime.now());
    order.setUser(user);
    when(ordersRepository.findById(any())).thenReturn(Optional.of(order));

    Order loadedOrder = ordersService.getOrder(1L);

    assertEquals(order, loadedOrder);
    verify(ordersRepository).findById(any());
  }

  @Test
  public void testInexistentOrderIsOwner() {
    OrdersRepository ordersRepository = mock(OrdersRepository.class);
    OrderMapper orderMapper = mock(OrderMapper.class);
    OrdersService ordersService = new OrdersService(ordersRepository, orderMapper);

    when(ordersRepository.findById(any())).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> ordersService.isOwner(1L, "1"));
    verify(ordersRepository).findById(any());
  }

  @Test
  public void testOrderIsOwner() {
    OrdersRepository ordersRepository = mock(OrdersRepository.class);
    OrderMapper orderMapper = mock(OrderMapper.class);
    OrdersService ordersService = new OrdersService(ordersRepository, orderMapper);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
    passwordEncoder.encode("pass"), "REGISTERED_USER");
    user.setId(1L);

    Order order = new Order(new User(), OrderStatus.FINISHED, "example_address", LocalDateTime.now());
    order.setUser(user);
    when(ordersRepository.findById(any())).thenReturn(Optional.of(order));

    assertTrue(ordersService.isOwner(1L, "1"));
    verify(ordersRepository).findById(any());
  }

  @Test
  public void testOrderIsNotOwner() {
    OrdersRepository ordersRepository = mock(OrdersRepository.class);
    OrderMapper orderMapper = mock(OrderMapper.class);
    OrdersService ordersService = new OrdersService(ordersRepository, orderMapper);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
    passwordEncoder.encode("pass"), "REGISTERED_USER");
    user.setId(1L);

    User otherUser = new User("other", "test", "otherTest", "c/otheraddress", "otheruser@example.com",
    passwordEncoder.encode("pass"), "REGISTERED_USER");
    otherUser.setId(2L);

    Order order = new Order(new User(), OrderStatus.FINISHED, "example_address", LocalDateTime.now());
    order.setUser(otherUser);
    when(ordersRepository.findById(any())).thenReturn(Optional.of(order));

    assertFalse(ordersService.isOwner(1L, "1"));
    verify(ordersRepository).findById(any());
  }

}
