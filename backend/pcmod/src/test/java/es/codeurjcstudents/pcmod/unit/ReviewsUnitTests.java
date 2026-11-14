package es.codeurjcstudents.pcmod.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import es.codeurjcstudents.pcmod.dto.ReviewMapper;
import es.codeurjcstudents.pcmod.enums.ComponentType;
import es.codeurjcstudents.pcmod.model.Component;
import es.codeurjcstudents.pcmod.model.Review;
import es.codeurjcstudents.pcmod.model.User;
import es.codeurjcstudents.pcmod.repository.ComponentsRepository;
import es.codeurjcstudents.pcmod.repository.ReviewsRepository;
import es.codeurjcstudents.pcmod.repository.UsersRepository;
import es.codeurjcstudents.pcmod.service.ReviewsService;

@Tag("server-unit")
public class ReviewsUnitTests {

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Test
  void testCreateReview() {
    ComponentsRepository componentRepository = mock(ComponentsRepository.class);
    UsersRepository userRepository = mock(UsersRepository.class);
    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewMapper reviewMapper = mock(ReviewMapper.class);
    
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, reviewMapper, componentRepository, userRepository);
    
    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        passwordEncoder.encode("pass"), "REGISTERED_USER");
    Component component = new Component("sample_name", "sample_description", ComponentType.CPU,
    "sample_brand", BigDecimal.valueOf(100.00), 0);
    when(userRepository.findById(any())).thenReturn(Optional.of(user));
    when(componentRepository.findById(any())).thenReturn(Optional.of(component));

    Review reviewToCreate = new Review("sample_title", "sample_message", 5);

    when(reviewsRepository.save(any())).thenReturn(reviewToCreate);

    Review savedReview = reviewsService.createReview(1L, 1L, reviewToCreate);
    
    assertEquals(reviewToCreate.getTitle(), savedReview.getTitle());
    assertEquals(reviewToCreate.getMessage(), savedReview.getMessage());
    assertEquals(reviewToCreate.getRating(), savedReview.getRating());
  }

  @Test
  void testCreateReviewForNoExistingComponent() {
    ComponentsRepository componentRepository = mock(ComponentsRepository.class);
    UsersRepository userRepository = mock(UsersRepository.class);
    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewMapper reviewMapper = mock(ReviewMapper.class);
    
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, reviewMapper, componentRepository, userRepository);
    
    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        passwordEncoder.encode("pass"), "REGISTERED_USER");
    when(userRepository.findById(any())).thenReturn(Optional.of(user));
    when(componentRepository.findById(any())).thenReturn(Optional.empty());

    Review reviewToCreate = new Review("sample_title", "sample_message", 5);

    assertThrows(ResponseStatusException.class, () -> {
      reviewsService.createReview(1L, 1L, reviewToCreate);
    });
  }

  @Test
  void testCreateReviewForNoExistingUser() {
    ComponentsRepository componentRepository = mock(ComponentsRepository.class);
    UsersRepository userRepository = mock(UsersRepository.class);
    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewMapper reviewMapper = mock(ReviewMapper.class);
    
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, reviewMapper, componentRepository, userRepository);

    Component component = new Component("sample_name", "sample_description", ComponentType.CPU,
    "sample_brand", BigDecimal.valueOf(100.00), 0);
    when(userRepository.findById(any())).thenReturn(Optional.empty());
    when(componentRepository.findById(any())).thenReturn(Optional.of(component));

    Review reviewToCreate = new Review("sample_title", "sample_message", 5);

    assertThrows(ResponseStatusException.class, () -> {
      reviewsService.createReview(1L, 1L, reviewToCreate);
    });
  }

  @Test
  void testGetReviews() {
    ComponentsRepository componentRepository = mock(ComponentsRepository.class);
    UsersRepository userRepository = mock(UsersRepository.class);
    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewMapper reviewMapper = mock(ReviewMapper.class);
    
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, reviewMapper, componentRepository, userRepository);
    Pageable pageable = mock(Pageable.class);
    
    List<Review> reviewsList = Arrays.asList(new Review(), new Review());
    Page<Review> expectedPage = new PageImpl<>(reviewsList);
    when(reviewsRepository.findAllByComponentId(any(), eq(pageable))).thenReturn(expectedPage);

    Page<Review> actualPage = reviewsService.findAllByComponent(1L, pageable);

    assertEquals(expectedPage, actualPage);
    verify(reviewsRepository).findAllByComponentId(1L, pageable);

  }

  @Test
  void testGetZeroReviews() {
    ComponentsRepository componentRepository = mock(ComponentsRepository.class);
    UsersRepository userRepository = mock(UsersRepository.class);
    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewMapper reviewMapper = mock(ReviewMapper.class);
    
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, reviewMapper, componentRepository, userRepository);
    Pageable pageable = mock(Pageable.class);
    
    List<Review> reviewsList = Arrays.asList();
    Page<Review> expectedPage = new PageImpl<>(reviewsList);
    when(reviewsRepository.findAllByComponentId(any(), eq(pageable))).thenReturn(expectedPage);

    Page<Review> actualPage = reviewsService.findAllByComponent(1L, pageable);

    assertEquals(expectedPage, actualPage);
    verify(reviewsRepository).findAllByComponentId(1L, pageable);
  }

  @Test
  public void testSaveReview() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    UsersRepository usersRepository = mock(UsersRepository.class);
    ReviewMapper reviewMapper = mock(ReviewMapper.class);
    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, reviewMapper, componentsRepository, usersRepository );

    Component component = new Component("sample_name", "sample_message", ComponentType.CPU, "sample_brand", BigDecimal.valueOf(100.00), 5);
    component.setId(1L);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
      passwordEncoder.encode("pass"), "REGISTERED_USER");

    Review review = new Review("sample_title", "sample_message", 5);

    when(componentsRepository.findById(1L)).thenReturn(Optional.of(component));
    when(reviewsRepository.existsByComponentIdAndUserId(1L, 1L)).thenReturn(false);
    when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
    when(reviewsRepository.findById(1L)).thenReturn(Optional.of(review)); 
    when(reviewsRepository.save(any(Review.class))).thenReturn(review);

    Review savedReview = reviewsService.save(review, 1L, 1L);
    
    assertEquals(review.getTitle(), savedReview.getTitle());
    assertEquals(review.getMessage(), savedReview.getMessage());
    assertEquals(review.getRating(), savedReview.getRating());
    verify(reviewsRepository).save(any(Review.class));
  }

  @Test
  public void testSaveReviewInComponentExistingSameUserReview() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    UsersRepository usersRepository = mock(UsersRepository.class);
    ReviewMapper reviewMapper = mock(ReviewMapper.class);
    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, reviewMapper, componentsRepository, usersRepository );

    Component component = new Component("sample_name", "sample_message", ComponentType.CPU, "sample_brand", BigDecimal.valueOf(100.00), 5);
    component.setId(1L);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
      passwordEncoder.encode("pass"), "REGISTERED_USER");

    Review review = new Review("sample_title", "sample_message", 5);

    when(componentsRepository.findById(1L)).thenReturn(Optional.of(component));
    when(reviewsRepository.existsByComponentIdAndUserId(1L, 1L)).thenReturn(true);
    when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
    when(reviewsRepository.findById(1L)).thenReturn(Optional.of(review)); 

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> reviewsService.save(review, 1L, 1L));
    assertEquals("Validación fallida:\n• Ya existe una reseña de este producto para tu usuario.", exception.getMessage());
    verify(reviewsRepository, never()).save(any(Review.class));
  }

  @Test
  public void testSaveVoidReviewData() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    UsersRepository usersRepository = mock(UsersRepository.class);
    ReviewMapper reviewMapper = mock(ReviewMapper.class);
    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, reviewMapper, componentsRepository, usersRepository );

    Component component = new Component("sample_name", "sample_message", ComponentType.CPU, "sample_brand", BigDecimal.valueOf(100.00), 5);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
      passwordEncoder.encode("pass"), "REGISTERED_USER");

    Review review = new Review("", "", null);

    when(componentsRepository.findById(1L)).thenReturn(Optional.of(component));
    when(reviewsRepository.existsByComponentIdAndUserId(1L, 1L)).thenReturn(false);
    when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
    when(reviewsRepository.findById(1L)).thenReturn(Optional.of(review)); 

    assertThrows(IllegalArgumentException.class, () -> reviewsService.save(review, 1L, 1L));
    verify(reviewsRepository, never()).save(any(Review.class));

  }

  @Test
  public void testSaveNullReviewData() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    UsersRepository usersRepository = mock(UsersRepository.class);
    ReviewMapper reviewMapper = mock(ReviewMapper.class);
    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, reviewMapper, componentsRepository, usersRepository );

    Component component = new Component("sample_name", "sample_message", ComponentType.CPU, "sample_brand", BigDecimal.valueOf(100.00), 5);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
      passwordEncoder.encode("pass"), "REGISTERED_USER");

    Review review = new Review(null, null, null);

    when(componentsRepository.findById(1L)).thenReturn(Optional.of(component));
    when(reviewsRepository.existsByComponentIdAndUserId(1L, 1L)).thenReturn(false);
    when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
    when(reviewsRepository.findById(1L)).thenReturn(Optional.of(review)); 

    assertThrows(IllegalArgumentException.class, () -> reviewsService.save(review, 1L, 1L));
    verify(reviewsRepository, never()).save(any(Review.class));

  }

  @Test
  public void testSaveBadNumReviewData() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    UsersRepository usersRepository = mock(UsersRepository.class);
    ReviewMapper reviewMapper = mock(ReviewMapper.class);
    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, reviewMapper, componentsRepository, usersRepository );

    Component component = new Component("sample_name", "sample_message", ComponentType.CPU, "sample_brand", BigDecimal.valueOf(100.00), 5);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
      passwordEncoder.encode("pass"), "REGISTERED_USER");

    Review review = new Review("sample_title", "sample_message", -5);

    when(componentsRepository.findById(1L)).thenReturn(Optional.of(component));
    when(reviewsRepository.existsByComponentIdAndUserId(1L, 1L)).thenReturn(false);
    when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
    when(reviewsRepository.findById(1L)).thenReturn(Optional.of(review)); 

    assertThrows(IllegalArgumentException.class, () -> reviewsService.save(review, 1L, 1L));
    verify(reviewsRepository, never()).save(any(Review.class));

  }

  @Test
  public void testReviewValidation() {

    ReviewsService reviewsService = new ReviewsService(null, null, null, null );

    Review review = new Review("sample_title", "sample_message", 5);

    List<String> errors = reviewsService.validateReview(review);

    assertTrue(errors.isEmpty());

  }

  @Test
  public void testVoidReviewValidation() {

    ReviewsService reviewsService = new ReviewsService(null, null, null, null );

    Review review = new Review("", "", null);

    List<String> errors = reviewsService.validateReview(review);

    assertTrue(errors.contains("El título es obligatorio."));
    assertTrue(errors.contains("El mensaje es obligatorio."));
    assertTrue(errors.contains("La valoración es obligatoria."));

  }

  @Test
  public void testNullReviewValidation() {

    ReviewsService reviewsService = new ReviewsService(null, null, null, null );

    Review review = new Review(null, null, null);

    List<String> errors = reviewsService.validateReview(review);

    assertTrue(errors.contains("El título es obligatorio."));
    assertTrue(errors.contains("El mensaje es obligatorio."));
    assertTrue(errors.contains("La valoración es obligatoria."));

  }

  @Test
  public void testBadNumReviewValidation() {

    ReviewsService reviewsService = new ReviewsService(null, null, null, null );

    Review review = new Review("sample_title", "sample_message", -5);

    List<String> errors = reviewsService.validateReview(review);

    assertTrue(errors.contains("La valoración del producto ha de estar entre 1 y 5."));

  }

  @Test
  public void testHasUserReviewed() {

    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, null, null, null );

    when(reviewsRepository.existsByComponentIdAndUserId(any(), any())).thenReturn(true); 

    boolean res = reviewsService.hasUserReviewed(1L, 1L);

    assertEquals(true, res);
    verify(reviewsRepository).existsByComponentIdAndUserId(1L, 1L);

  }

  @Test
  public void testHasNotUserReviewed() {

    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, null, null, null );

    when(reviewsRepository.existsByComponentIdAndUserId(any(), any())).thenReturn(false); 

    boolean res = reviewsService.hasUserReviewed(1L, 1L);

    assertEquals(false, res);
    verify(reviewsRepository).existsByComponentIdAndUserId(1L, 1L);

  }

  @Test
  public void testDeleteReview() {

    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, null, null, null );
    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);

    Component component = new Component("sample_name", "sample_message", ComponentType.CPU, "sample_brand", BigDecimal.valueOf(100.00), 5);
    component.setId(1L);

    Review review = new Review("sample_title", "sample_message", 5);
    review.setComponent(component);

    when(reviewsRepository.findById(1L)).thenReturn(java.util.Optional.of(review)); 
    when(componentsRepository.findById(1L)).thenReturn(java.util.Optional.of(component));

    reviewsService.delete(1L, 1L);

    verify(reviewsRepository).deleteById(1L);

  }

  @Test
  public void testDeleteReviewNotFound() {

    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, null, null, null );
    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);

    Component component = new Component("sample_name", "sample_message", ComponentType.CPU, "sample_brand", BigDecimal.valueOf(100.00), 5);

    when(reviewsRepository.findById(1L)).thenReturn(java.util.Optional.empty()); 
    when(componentsRepository.findById(1L)).thenReturn(java.util.Optional.of(component));

    reviewsService.delete(1L, 1L);

    verify(reviewsRepository, never()).deleteById(1L);

  }

  @Test
  public void testDeleteReviewOfWrongComponent() {

    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, null, null, null );
    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);

    Component component = new Component("sample_name", "sample_message", ComponentType.CPU, "sample_brand", BigDecimal.valueOf(100.00), 5);
    Component otherComponent = new Component("sample_name2", "sample_message2", ComponentType.CPU, "sample_brand2", BigDecimal.valueOf(100.00), 5);
    component.setId(1L);
    otherComponent.setId(2L);

    Review review = new Review("sample_title", "sample_message", 5);
    review.setComponent(component);

    when(reviewsRepository.findById(1L)).thenReturn(java.util.Optional.of(review)); 
    when(componentsRepository.findById(2L)).thenReturn(java.util.Optional.of(otherComponent));

    assertThrows(NoSuchElementException.class, () -> reviewsService.delete(2L, 1L));
    verify(reviewsRepository, never()).deleteById(1L);

  }

  @Test 
  public void testIsOwner() {

    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, null, null, null );
    UsersRepository usersRepository = mock(UsersRepository.class);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        null, "");
    user.setId(1L);

    Review review = new Review("sample_title", "sample_message", 5);
    review.setUser(user);

    when(usersRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
    when(reviewsRepository.findById(1L)).thenReturn(java.util.Optional.of(review));

    assertTrue(reviewsService.isOwner(1L, "1"));

  }

  @Test 
  public void testIsNotOwner() {

    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, null, null, null );
    UsersRepository usersRepository = mock(UsersRepository.class);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        null, "");
    User otherUser = new User("user2", "test2", "userTest2", "c/testaddress", "testuser2@example.com",
        null, "");
    user.setId(1L);
    otherUser.setId(2L);

    Review review = new Review("sample_title", "sample_message", 5);
    review.setUser(user);

    when(usersRepository.findById(2L)).thenReturn(java.util.Optional.of(otherUser));
    when(reviewsRepository.findById(1L)).thenReturn(java.util.Optional.of(review));

    assertFalse(reviewsService.isOwner(1L, "2"));

  }

  @Test 
  public void testIsOwnerWithNoPresentReview() {

    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, null, null, null );
    UsersRepository usersRepository = mock(UsersRepository.class);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
        null, "");
    user.setId(1L);

    when(usersRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
    when(reviewsRepository.findById(1L)).thenReturn(java.util.Optional.empty());

    assertFalse(reviewsService.isOwner(1L, "1"));

  }

  @Test
  public void testUpdateReview() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    UsersRepository usersRepository = mock(UsersRepository.class);
    ReviewMapper reviewMapper = mock(ReviewMapper.class);
    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, reviewMapper, componentsRepository, usersRepository );

    Component component = new Component("sample_name", "sample_message", ComponentType.CPU, "sample_brand", BigDecimal.valueOf(100.00), 5);
    component.setId(1L);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
      passwordEncoder.encode("pass"), "REGISTERED_USER");

    Review review = new Review("sample_title", "sample_message", 5);
    review.setComponent(component);
    Review reviewToUpdate = new Review("sample_title_updated", "sample_message_updated", 1);

    when(componentsRepository.findById(1L)).thenReturn(Optional.of(component));
    when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
    when(reviewsRepository.findById(1L)).thenReturn(Optional.of(review)); 
    when(reviewsRepository.save(any(Review.class))).thenReturn(review);

    Review updatedReview = reviewsService.updateReview(reviewToUpdate, 1L, 1L);

    assertEquals(reviewToUpdate.getTitle(), updatedReview.getTitle());
    assertEquals(reviewToUpdate.getMessage(), updatedReview.getMessage());
    assertEquals(reviewToUpdate.getRating(), updatedReview.getRating());
    verify(reviewsRepository).save(any(Review.class));
  }

  @Test
  public void testUpdateVoidReviewData() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    UsersRepository usersRepository = mock(UsersRepository.class);
    ReviewMapper reviewMapper = mock(ReviewMapper.class);
    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, reviewMapper, componentsRepository, usersRepository );

    Component component = new Component("sample_name", "sample_message", ComponentType.CPU, "sample_brand", BigDecimal.valueOf(100.00), 5);
    component.setId(1L);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
      passwordEncoder.encode("pass"), "REGISTERED_USER");

    Review review = new Review("", "", null);
    review.setComponent(component);

    when(componentsRepository.findById(1L)).thenReturn(Optional.of(component));
    when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
    when(reviewsRepository.findById(1L)).thenReturn(Optional.of(review)); 

    assertThrows(IllegalArgumentException.class, () -> reviewsService.updateReview(review, 1L, 1L));
    verify(reviewsRepository, never()).save(any(Review.class));

  }

  @Test
  public void testUpdateNullReviewData() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    UsersRepository usersRepository = mock(UsersRepository.class);
    ReviewMapper reviewMapper = mock(ReviewMapper.class);
    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, reviewMapper, componentsRepository, usersRepository );

    Component component = new Component("sample_name", "sample_message", ComponentType.CPU, "sample_brand", BigDecimal.valueOf(100.00), 5);
    component.setId(1L);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
      passwordEncoder.encode("pass"), "REGISTERED_USER");

    Review review = new Review(null, null, null);
    review.setComponent(component);

    when(componentsRepository.findById(1L)).thenReturn(Optional.of(component));
    when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
    when(reviewsRepository.findById(1L)).thenReturn(Optional.of(review)); 

    assertThrows(IllegalArgumentException.class, () -> reviewsService.updateReview(review, 1L, 1L));
    verify(reviewsRepository, never()).save(any(Review.class));

  }

  @Test
  public void testUpdateBadNumReviewData() {

    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    UsersRepository usersRepository = mock(UsersRepository.class);
    ReviewMapper reviewMapper = mock(ReviewMapper.class);
    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, reviewMapper, componentsRepository, usersRepository );

    Component component = new Component("sample_name", "sample_message", ComponentType.CPU, "sample_brand", BigDecimal.valueOf(100.00), 5);
    component.setId(1L);

    User user = new User("user", "test", "userTest", "c/testaddress", "testuser@example.com",
      passwordEncoder.encode("pass"), "REGISTERED_USER");

    Review review = new Review("sample_title", "sample_message", -5);
    review.setComponent(component);

    when(componentsRepository.findById(1L)).thenReturn(Optional.of(component));
    when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
    when(reviewsRepository.findById(1L)).thenReturn(Optional.of(review)); 

    assertThrows(IllegalArgumentException.class, () -> reviewsService.updateReview(review, 1L, 1L));
    verify(reviewsRepository, never()).save(any(Review.class));

  }

  @Test
  public void testUpdateReviewOfWrongComponent() {

    ReviewsRepository reviewsRepository = mock(ReviewsRepository.class);
    ReviewsService reviewsService = new ReviewsService(reviewsRepository, null, null, null );
    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);

    Component component = new Component("sample_name", "sample_message", ComponentType.CPU, "sample_brand", BigDecimal.valueOf(100.00), 5);
    Component otherComponent = new Component("sample_name2", "sample_message2", ComponentType.CPU, "sample_brand2", BigDecimal.valueOf(100.00), 5);
    component.setId(1L);
    otherComponent.setId(2L);

    Review review = new Review("sample_title", "sample_message", 5);
    review.setComponent(component);

    Review updatedReview = new Review("sample_title_updated", "sample_message_updated", Integer.valueOf(1));

    when(reviewsRepository.findById(1L)).thenReturn(java.util.Optional.of(review)); 
    when(componentsRepository.findById(2L)).thenReturn(java.util.Optional.of(otherComponent));

    assertThrows(NoSuchElementException.class, () -> reviewsService.updateReview(updatedReview, 2L, 1L));
    verify(reviewsRepository, never()).deleteById(1L);

  }
}
