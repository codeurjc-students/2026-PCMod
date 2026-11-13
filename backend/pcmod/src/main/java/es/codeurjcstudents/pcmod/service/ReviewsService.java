package es.codeurjcstudents.pcmod.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import es.codeurjcstudents.pcmod.dto.ReviewDTO;
import es.codeurjcstudents.pcmod.dto.ReviewMapper;
import es.codeurjcstudents.pcmod.model.Component;
import es.codeurjcstudents.pcmod.model.Review;
import es.codeurjcstudents.pcmod.model.User;
import es.codeurjcstudents.pcmod.repository.ComponentsRepository;
import es.codeurjcstudents.pcmod.repository.ReviewsRepository;
import es.codeurjcstudents.pcmod.repository.UsersRepository;
import jakarta.transaction.Transactional;

@Service
public class ReviewsService {

  private final ReviewsRepository reviewRepository;
  private final ReviewMapper reviewMapper;
  private final ComponentsRepository componentRepository;
  private final UsersRepository userRepository;

  public ReviewsService(ReviewsRepository reviewsRepository, ReviewMapper reviewMapper, ComponentsRepository componentRepository,
      UsersRepository userRepository) {
    this.reviewRepository = reviewsRepository;
    this.reviewMapper = reviewMapper;
    this.componentRepository = componentRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public Review createReview(Long userId, Long componentId, Review review) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

    Component component = componentRepository.findById(componentId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Componente no encontrado."));

    user.addReview(review);
    component.addReview(review);

    return reviewRepository.save(review);
  }

  public Page<Review> findAllByComponent(long id, Pageable pageable) {
    return reviewRepository.findAllByComponentId(id, pageable);
  }

  @PreAuthorize("hasRole('REGISTERED_USER') and (#userId.toString() == principal.username)")
  @Transactional
  public Review save(Review review, long userId, long componentId) {

    if (reviewRepository.existsByComponentIdAndUserId(componentId, userId)) {
      throw new IllegalArgumentException("Validación fallida:\n• Ya existe una reseña de este producto para tu usuario.");
    }

    List<String> errors = validateReview(review);

    if (!errors.isEmpty()) {
      throw new IllegalArgumentException("Validación fallida:\n• " + String.join("\n• ", errors));
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

    Component component = componentRepository.findById(componentId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Componente no encontrado."));

    review.setDate(LocalDate.now(ZoneId.of("Europe/Madrid")));
    user.addReview(review);
    component.addReview(review);

    return reviewRepository.save(review);
  }

  @PreAuthorize("hasRole('ADMIN') or @reviewsService.isOwner(#reviewId, principal.username)")
  public void delete(long componentId, long reviewId) {
    
    Optional<Review> reviewToDelete = reviewRepository.findById(reviewId);

    if ((reviewToDelete.isPresent()) && (reviewToDelete.get().getComponent().getId() != componentId)) {
      throw new NoSuchElementException("La reseña no pertenece a ese componente.");
    }

    if (reviewToDelete.isPresent()) {

      reviewRepository.deleteById(reviewId);

    }
    
  }

  @PreAuthorize("hasRole('ADMIN') or @reviewsService.isOwner(#reviewId, principal.username)")
  public Review updateReview(Review newReviewData, long componentId, long reviewId) {

    Optional<Review> reviewToUpdate = reviewRepository.findById(reviewId);

    if ((reviewToUpdate.isPresent()) && (reviewToUpdate.get().getComponent().getId() != componentId)) {
      throw new NoSuchElementException("La reseña no pertenece a ese componente.");
    }

    List<String> errors = validateReview(newReviewData);

    if (!errors.isEmpty()) {
      throw new IllegalArgumentException("Validación fallida:\n• " + String.join("\n• ", errors));
    }

    Review review = reviewRepository.findById(reviewId).orElseThrow();
    review.setTitle(newReviewData.getTitle());
    review.setMessage(newReviewData.getMessage());
    review.setRating(newReviewData.getRating());
    review.setDate(LocalDate.now(ZoneId.of("Europe/Madrid")));

    return reviewRepository.save(review);

  }

  public boolean hasUserReviewed(Long componentId, Long userId) {
    return reviewRepository.existsByComponentIdAndUserId(componentId, userId);
  }

  public List<String> validateReview(Review review) {

    List<String> errors = new ArrayList<>();

    validateRequiredField(review.getTitle(), "El título es obligatorio.", errors);
    validateRequiredField(review.getMessage(), "El mensaje es obligatorio.", errors);

    if (review.getRating() == null || review.getRating().toString().isEmpty()) {
      errors.add("La valoración es obligatoria.");
    } else {
      validateRating(review, errors);
    }

    return errors;

  }

  private void validateRequiredField(String value, String message, List<String> errors) {

    if (value == null || value.isEmpty()) {
      errors.add(message);
    }

  }

  public void validateRating(Review review, List<String> errors) {

    if ((review.getRating() < 1) || (review.getRating() > 5)) {
      errors.add("La valoración del producto ha de estar entre 1 y 5.");
    }

  }

  public boolean isOwner(long reviewId, String usernameId) {

    Optional<Review> review = reviewRepository.findById(reviewId);

    return review.isPresent() && (review.get().getUser().getId().equals(Long.valueOf(usernameId)));
  }

  public ReviewDTO toDTO(Review review) {
    return reviewMapper.toDTO(review);
  }

  public Review toDomain(ReviewDTO reviewDTO) {
    return reviewMapper.toDomain(reviewDTO);
  }

}
