package es.codeurjcstudents.pcmod.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import es.codeurjcstudents.pcmod.enums.ComponentType;
import es.codeurjcstudents.pcmod.model.Cart;
import es.codeurjcstudents.pcmod.model.CartItem;
import es.codeurjcstudents.pcmod.model.Component;
import es.codeurjcstudents.pcmod.model.User;
import es.codeurjcstudents.pcmod.repository.CartRepository;
import es.codeurjcstudents.pcmod.repository.ComponentsRepository;
import es.codeurjcstudents.pcmod.repository.OrdersRepository;
import es.codeurjcstudents.pcmod.repository.UsersRepository;
import es.codeurjcstudents.pcmod.service.CartService;

@Tag("server-integration")
@SpringBootTest
@Testcontainers
public class CartIntegrationTests {

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
  private CartService cartService;
  
  @Autowired 
  private CartRepository cartRepository;

  @Autowired
  private OrdersRepository orderRepository;

  @Autowired
  private ComponentsRepository componentsRepository;

  @Autowired
  private UsersRepository usersRepository;

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @BeforeEach
  void setUp() {
    orderRepository.deleteAll();
    cartRepository.deleteAll();
    usersRepository.deleteAll();
    componentsRepository.deleteAll();

    Component component1 = componentsRepository.save(new Component("AMD Ryzen 7 7800X3D",
        "8 núcleos y 16 hilos ideales para gaming\r\n" + //
            "96 MB de caché L3 3D V-Cache, baja latencia\r\n" + //
            "Compatible con memorias DDR5, doble canal\r\n" + //
            "Frecuencia base 4.2 GHz y turbo hasta 5.0 GHz",
        ComponentType.CPU, "AMD", BigDecimal.valueOf(350.00), 5));

    SecurityContextHolder.clearContext();
    User user = usersRepository.save(new User("user1", "test", "userTest1", "c/testaddress", "testuser1@example.com",
        passwordEncoder.encode("pass"), "REGISTERED_USER"));
    User admin = usersRepository.save(new User("admin", "test", "adminTest1", "c/testaddress", "testadmin1@example.com",
        passwordEncoder.encode("adminpass"), "REGISTERED_USER", "ADMIN"));

    Cart cart = new Cart(user);
    user.setCart(cart);
    cart.addItem(new CartItem(cart, component1, 2));
    cartRepository.save(cart);
  }

  @Test
  void testGetCartAsNoLoggedIn() {
    assertThrows(NullPointerException.class, () -> cartService.getCart(null));
  }

  @Test
  void testGetCartAsLoggedIn() {
    User user = usersRepository.findByEmail("testuser1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_REGISTERED_USER").build(),
            null, List.of(new SimpleGrantedAuthority("ROLE_REGISTERED_USER"))));
    Cart cart =assertDoesNotThrow(() -> cartService.getCart(SecurityContextHolder.getContext().getAuthentication()));
    assertEquals(1, cart.getItems().size());
  }

  @Test
  void testGetCartAsLoggedInWithoutExisitngCart() {
    User user = usersRepository.findByEmail("testadmin1@example.com").orElseThrow();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("").authorities("ROLE_ADMIN").build(),
            null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    Cart cart = assertDoesNotThrow(() -> cartService.getCart(SecurityContextHolder.getContext().getAuthentication()));
    assertEquals(0, cart.getItems().size());
  }
  
}
