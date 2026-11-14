package es.codeurjcstudents.pcmod.controller;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

import java.net.URI;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjcstudents.pcmod.dto.ReviewDTO;
import es.codeurjcstudents.pcmod.model.Review;
import es.codeurjcstudents.pcmod.service.ReviewsService;

@RestController
@RequestMapping("/api/v1/components/{id}/reviews")
public class ReviewsRestController {

  @Autowired
  private ReviewsService reviewsService;

  @GetMapping("/")
  public Page<ReviewDTO> findAllByComponent(@PathVariable long id, Pageable pageable) {
    return reviewsService.findAllByComponent(id, pageable).map(reviewsService::toDTO);
  }

  @GetMapping("/has-reviewed")
  public ResponseEntity<Boolean> hasUserReviewed(@PathVariable long id, Principal principal) {

    if (principal == null) {
      return ResponseEntity.ok(false);
    }

    return ResponseEntity.ok(reviewsService.hasUserReviewed(id, Long.parseLong(principal.getName())));
  }

  @PostMapping("/")
  public ResponseEntity<ReviewDTO> createReview(@RequestBody ReviewDTO reviewDTO, @PathVariable long id, Principal principal) {

    Review review = reviewsService.toDomain(reviewDTO);

    reviewsService.save(review, Long.valueOf(principal.getName()), id);

    URI location = fromCurrentRequest().path("/{id}").buildAndExpand(review.getId()).toUri();

    return ResponseEntity.created(location).body(reviewsService.toDTO(review));
  }

  @DeleteMapping("/{reviewId}")
  public void deleteReview(@PathVariable long id, @PathVariable long reviewId) {
    reviewsService.delete(id, reviewId);
  }

  @PutMapping("/{reviewId}")
  public ResponseEntity<ReviewDTO> updateComponent(@RequestBody ReviewDTO updatedData, @PathVariable long id, @PathVariable long reviewId) {

    Review newReviewData = reviewsService.toDomain(updatedData);
    Review updatedReview = reviewsService.updateReview(newReviewData, id, reviewId);

    URI location = fromCurrentRequest().buildAndExpand(updatedReview.getId()).toUri();

    return ResponseEntity.created(location).body(reviewsService.toDTO(updatedReview));

  }

}
