import type { Route } from "./+types/detail-order";
import { Link } from "react-router";
import { ArrowLeft } from "react-bootstrap-icons";
import { getOrder } from "~/services/orders-service";

export async function clientLoader({ params }: Route.ClientLoaderArgs) {
  const order = await getOrder(Number(params.id));
  return { order };
}

export default function OrderDetail({ loaderData }: Route.ComponentProps) {

  const formatDate = (date: string) => {
    const d = new Date(date);

    return d.toLocaleString("es-ES", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
        hour12: false,
        timeZone: "Europe/Madrid"
    }).replace(", ", "-");
  };

  const order = loaderData.order;

  return (
      <main className="main">
        <div className="container">
          <div className="order-detail-card">
            <div className="order-data">
              <Link to="/me">
                <ArrowLeft />
                <span className="ms-2">Volver al perfil</span>
              </Link>
              <h2 id="order-title">Pedido #{order.id}-{formatDate(order.createdAt)}-{order.userUsername}</h2>
              <h3 id="order-user">Cliente: {order.userUsername}</h3>
              <h3 id="order-date">Fecha de compra: {formatDate(order.createdAt)}</h3>
              <h3 id="order-address">Dirección de entrega: {order.shippingAddress}</h3>
              <h3 id="order-total">Precio total del pedido: {order.totalPrice.toFixed(2)} €</h3>
            </div>
            <div className="order-components">
              <h3 id="order-components-title">Componentes del pedido:</h3>
              {order.items.map((item) => (
                <div className="order-item" key={item.id}>
                  <Link to={`/components/${item.component.id}`} className="item-card-link">
                    <h3 id={`item-${item.id}-name`} className={`item-${item.component.id}-name`}>{item.component.name}</h3>
                  </Link>
                  <h4 id={`item-${item.id}-brand-type`}>{item.component.brand} | {item.component.type}</h4>
                  <h4 id={`item-${item.id}-quantity`}>Cantidad: {item.quantity}</h4>
                  <h4 id={`item-${item.id}-price`}>Precio unitario: {item.component.price.toFixed(2)} €</h4>
                  <h4 id={`item-${item.id}-total`}>Precio total del producto: {(item.component.price * item.quantity).toFixed(2)} €</h4>
                </div>
              ))}
            </div>
          </div>
        </div>
      </main>
  );

}