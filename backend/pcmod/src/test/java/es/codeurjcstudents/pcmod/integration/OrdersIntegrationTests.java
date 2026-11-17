package es.codeurjcstudents.pcmod.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import es.codeurjcstudents.pcmod.enums.ComponentType;
import es.codeurjcstudents.pcmod.enums.OrderStatus;
import es.codeurjcstudents.pcmod.model.Order;
import es.codeurjcstudents.pcmod.model.OrderItem;
import es.codeurjcstudents.pcmod.model.Component;
import es.codeurjcstudents.pcmod.model.User;
import es.codeurjcstudents.pcmod.repository.ComponentsRepository;
import es.codeurjcstudents.pcmod.repository.OrdersRepository;
import es.codeurjcstudents.pcmod.repository.UsersRepository;
import es.codeurjcstudents.pcmod.service.OrdersService;

@Tag("server-integration")
@SpringBootTest
@Testcontainers
public class OrdersIntegrationTests {

  @Container
  private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.4")
      .withDatabaseName("TestDB")
      .withUsername("TestDBUser")
      .withPassword("TestDBPassword");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url",
        () -> mysqlContainer.getJdbcUrl() + "?useSSL=false&allowPublicKeyRetrieval=true");

    registry.add("spring.datasource.username", mysqlContainer::getUsername);
    registry.add("spring.datasource.password", mysqlContainer::getPassword);
    registry.add("spring.datasource.driver-class-name", mysqlContainer::getDriverClassName);
  }

  @Autowired 
  private OrdersService ordersService;
  
  @Autowired 
  private OrdersRepository ordersRepository;

  @Autowired
  private ComponentsRepository componentsRepository;

  @Autowired
  private UsersRepository usersRepository;

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @BeforeEach
  void setUp() {
    ordersRepository.deleteAll();
    usersRepository.deleteAll();
    componentsRepository.deleteAll();

    Component component1 = componentsRepository.save(new Component("AMD Ryzen 7 7800X3D",
        "8 núcleos y 16 hilos ideales para gaming\r\n" + //
            "96 MB de caché L3 3D V-Cache, baja latencia\r\n" + //
            "Compatible con memorias DDR5, doble canal\r\n" + //
            "Frecuencia base 4.2 GHz y turbo hasta 5.0 GHz",
        ComponentType.CPU, "AMD", BigDecimal.valueOf(350.00), 5));

    Component component2 = componentsRepository.save(new Component("NVIDIA GeForce RTX 5060 WINDFORCE MAX OC",
        "Ray Tracing y DLSS 4 para gaming fluido\r\n" + //
            "8GB GDDR7 y arquitectura Blackwell\r\n" + //
            "WINDFORCE 2X: refrigeración eficiente y silenciosa\r\n" + //
            "DisplayPort 2.1 y HDMI 2.1b hasta 8K\r\n" + //
            "Tensor Cores 5ª gen y Reflex 2 para IA avanzada",
        ComponentType.GPU, "Gigabyte", BigDecimal.valueOf(359.85), 2));

    SecurityContextHolder.clearContext();
    User user = usersRepository.save(new User("user1", "test", "userTest1", "c/testaddress", "testuser1@example.com",
        passwordEncoder.encode("pass"), "REGISTERED_USER"));
    User admin = usersRepository.save(new User("admin", "test", "adminTest1", "c/testaddress", "testadmin1@example.com",
        passwordEncoder.encode("adminpass"), "REGISTERED_USER", "ADMIN"));


    Order order1 = ordersRepository.save(new Order(user, OrderStatus.FINISHED, user.getAddress(), LocalDateTime.now(ZoneId.of("Europe/Madrid"))));
    order1.addItem(new OrderItem(order1, component1, 2, BigDecimal.valueOf(350.00)));
    order1.addItem(new OrderItem(order1, component2, 1, BigDecimal.valueOf(359.85)));
    ordersRepository.save(order1);

    Order order2 = ordersRepository.save(new Order(admin, OrderStatus.FINISHED, user.getAddress(), LocalDateTime.now(ZoneId.of("Europe/Madrid"))));
    order2.addItem(new OrderItem(order2, component1, 1, BigDecimal.valueOf(350.00)));
    ordersRepository.save(order2);
  }

  @Test
  void testGetOrdersAsNoLoggedIn() {
    Pageable pageable = Pageable.ofSize(10).withPage(0);
    assertThrows(AuthenticationCredentialsNotFoundException.class, () -> ordersService.getOrdersByUser(usersRepository.findAll().getFirst(), pageable));
  }

  @Test
  void testGetOwnOrdersAsLoggedIn() {
    User user = usersRepository.findByEmail("testuser1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER").build(),
            null, List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"))));

    Pageable pageable = Pageable.ofSize(10).withPage(0);
    Page<Order> orders = assertDoesNotThrow(() -> ordersService.getOrdersByUser(usersRepository.findAll().getFirst(), pageable));
    assertEquals(1, orders.getNumberOfElements());
    assertEquals(2, orders.getContent().getFirst().getItems().size());
  }

  @Test
  void testGetOtherUserOrdersAsLoggedIn() {
    User user = usersRepository.findByEmail("testuser1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER").build(),
            null, List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"))));

    Pageable pageable = Pageable.ofSize(10).withPage(0);
    assertThrows(AuthorizationDeniedException.class, () -> ordersService.getOrdersByUser(usersRepository.findAll().getLast(), pageable));
  }

  @Test
  void testGetOrdersAsAdmin() {
    User user = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Pageable pageable = Pageable.ofSize(10).withPage(0);
    Page<Order> ordersUser = assertDoesNotThrow(() -> ordersService.getOrdersByUser(usersRepository.findAll().getFirst(), pageable));
    assertEquals(1, ordersUser.getNumberOfElements());
    assertEquals(2, ordersUser.getContent().getFirst().getItems().size());
    Page<Order> ordersAdmin = assertDoesNotThrow(() -> ordersService.getOrdersByUser(usersRepository.findAll().getLast(), pageable));
    assertEquals(1, ordersAdmin.getNumberOfElements());
    assertEquals(1, ordersAdmin.getContent().getFirst().getItems().size());
  }

  @Test
  void testGetOrderAsNoLoggedIn() {
    assertThrows(AuthenticationCredentialsNotFoundException.class, () -> ordersService.getOrder(ordersRepository.findAll().getFirst().getId()));
  }

  @Test
  void testGetOwnOrderAsLoggedIn() {
    User user = usersRepository.findByEmail("testuser1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER").build(),
            null, List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"))));

    Order order = assertDoesNotThrow(() -> ordersService.getOrder(ordersRepository.findAll().getFirst().getId()));
    assertEquals(2, order.getItems().size());
    assertEquals(user.getId(), order.getUser().getId());
    assertEquals(OrderStatus.FINISHED, order.getStatus());
    assertEquals(user.getAddress(), order.getShippingAddress());
  }

  @Test
  void testGetOtherUserOrderAsLoggedIn() {
    User user = usersRepository.findByEmail("testuser1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER").build(),
            null, List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"))));

    assertThrows(AuthorizationDeniedException.class, () -> ordersService.getOrder(ordersRepository.findAll().getLast().getId()));
  }

  @Test
  void testGetOrderAsAdmin() {
    User user = usersRepository.findByEmail("testuser1@example.com").orElseThrow();
    User admin = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(admin.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    Order userOrder =assertDoesNotThrow(() -> ordersService.getOrder(ordersRepository.findAll().getFirst().getId()));
    assertEquals(2, userOrder.getItems().size());
    assertEquals(user.getId(), userOrder.getUser().getId());
    assertEquals(OrderStatus.FINISHED, userOrder.getStatus());
    assertEquals(user.getAddress(), userOrder.getShippingAddress());
    Order adminOrder = assertDoesNotThrow(() -> ordersService.getOrder(ordersRepository.findAll().getLast().getId()));
    assertEquals(1, adminOrder.getItems().size());
    assertEquals(admin.getId(), adminOrder.getUser().getId());
    assertEquals(OrderStatus.FINISHED, adminOrder.getStatus());
    assertEquals(admin.getAddress(), adminOrder.getShippingAddress());
  }

  @Test
  void testGetInexistentOrder() {
    User user = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> ordersService.getOrder(99L));
    assertTrue(exception.getMessage().contains("Pedido no encontrado."));
  }

}