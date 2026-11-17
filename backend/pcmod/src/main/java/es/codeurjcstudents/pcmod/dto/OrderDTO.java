package es.codeurjcstudents.pcmod.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import es.codeurjcstudents.pcmod.enums.OrderStatus;

public record OrderDTO(
    Long id,
    OrderStatus status,
    BigDecimal totalPrice,
    String shippingAddress,
    LocalDateTime createdAt,
    String userUsername,
    List<OrderItemDTO> items
) {}