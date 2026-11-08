package es.codeurjcstudents.pcmod.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
