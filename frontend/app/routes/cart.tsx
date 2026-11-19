import { getCart } from "~/services/cart-service";
import type { Route } from "./+types/cart";
import { useNavigate } from "react-router";
import { ArrowLeft, Cart2, CartX } from "react-bootstrap-icons";
import { Button } from "react-bootstrap";
import CartItemCard from "~/components/cartItemCard";

export async function clientLoader() {
  return await getCart();
}

export default function Cart({ loaderData }: Route.ComponentProps) {

  const cart = loaderData;
  const navigate = useNavigate();

  return (
      <main className="main">
        <div className="container">
          <Button className="cart-back-button" onClick={() => navigate(-1)}>
            <ArrowLeft />
            <span className="ms-2">Volver</span>
          </Button>
          <h2 id="cart-title" className="cart-title">Resumen del carro de compra:</h2>
          <div className="cart-items">
            {cart.items.length > 0 ? (
              <>
                {cart.items.map((item) => (
                  <CartItemCard key={item.id} cartItem={item} />
                ))}
                <Button id="go-to-payment-button" className="go-to-payment-button">
                  <Cart2 />
                  <span id="go-to-payment" className="go-to-payment">Finalizar compra</span>
                </Button>
              </>
            ) : (
              <div className="empty-cart-state">
                <CartX />
                <h4 id="empty-cart" className="empty-cart">
                  No ha añadido productos al carro de compra todavía.
                </h4>
              </div>
            )}
          </div>
        </div>
      </main>
  );

}