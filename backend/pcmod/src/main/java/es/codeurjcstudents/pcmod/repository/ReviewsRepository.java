package es.codeurjcstudents.pcmod.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import es.codeurjcstudents.pcmod.model.Review;

public interface ReviewsRepository extends JpaRepository<Review, Long> {

  Page<Review> findAllByComponentId(Long id, Pageable pageable);

  boolean existsByComponentIdAndUserId(Long componentId, Long userId);

}