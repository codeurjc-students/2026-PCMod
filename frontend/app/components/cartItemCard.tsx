import { Dash, Plus } from "react-bootstrap-icons";
import { Link } from "react-router";
import type CartItemDTO from "~/dtos/CartItemDTO";

export default function CartItemCard({ cartItem }: Readonly<{ cartItem: CartItemDTO }>) {

  return (
    <div className="cartItem-card">
      <Link to={`/components/${cartItem.component.id}`} className="cartitem-card-link">
        <h3 id={`cartItem-name-${cartItem.id}`} className="cartItem-name">{cartItem.component.name}</h3>
      </Link>
      <div className="cartItem-information">  
        <h4 id={`cartItem-brand-type-${cartItem.id}`} className="cartItem-brand-type">{cartItem.component.brand} | {cartItem.component.type}</h4>
        <h4 id={`cartItem-unit-price-${cartItem.id}`} className="cartItem-unit-price">Precio unitario: {cartItem.component.price.toFixed(2)} €/unidad</h4>
      </div>
      <div className="quatity-selector">
        <button className="quantity-button">
          <Dash />
        </button>
        <span id={`cartItem-quantity-value-${cartItem.id}`} className="quantity-value">{cartItem.quantity}</span>
        <button className="quantity-button">
          <Plus />
        </button>
      </div>
      <div className="cartItem-price">
        <h3 id={`cartItem-price-${cartItem.id}`} className="cartItem-price-text">{(cartItem.component.price * cartItem.quantity).toFixed(2)} €</h3>
      </div>
    </div>
  );
}