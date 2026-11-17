package es.codeurjcstudents.pcmod.dto;

import org.mapstruct.Mapper;
import es.codeurjcstudents.pcmod.model.Order;
import es.codeurjcstudents.pcmod.model.OrderItem;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ComponentMapper.class})
public interface OrderMapper {

    OrderDTO toDTO(Order order);

    List<OrderDTO> toDTOList(List<Order> orders);

    OrderItemDTO toItemDTO(OrderItem orderItem);

    Order toDomain(OrderDTO orderDTO);
}