package es.codeurjcstudents.pcmod.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjcstudents.pcmod.dto.ReviewDTO;
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

}
