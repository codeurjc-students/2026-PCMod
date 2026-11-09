package es.codeurjcstudents.pcmod.service;

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
