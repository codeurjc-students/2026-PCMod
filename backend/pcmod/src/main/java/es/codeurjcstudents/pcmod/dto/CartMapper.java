package es.codeurjcstudents.pcmod.dto;

import org.mapstruct.Mapper;
import es.codeurjcstudents.pcmod.model.Cart;
import es.codeurjcstudents.pcmod.model.CartItem;

@Mapper(componentModel = "spring", uses = {ComponentMapper.class})
public interface CartMapper {

    CartDTO toDTO(Cart cart);

    CartItemDTO toItemDTO(CartItem cartItem);

    Cart toDomain(CartDTO cartDTO);
}