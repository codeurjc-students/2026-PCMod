package es.codeurjcstudents.pcmod.dto;

import java.math.BigDecimal;

public record OrderItemDTO(
    Long id,
    ComponentBasicDTO component,
    String componentName,
    int quantity,
    BigDecimal unitPriceAtPurchase
) {}