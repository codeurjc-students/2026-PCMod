package es.codeurjcstudents.pcmod.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import es.codeurjcstudents.pcmod.model.Order;
import es.codeurjcstudents.pcmod.model.User;

public interface OrdersRepository extends JpaRepository<Order, Long> {
    @Override
    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findById(Long id);

    @EntityGraph(attributePaths = {"items"})
    Page<Order>findAllByUser(User user, Pageable pageable);

}