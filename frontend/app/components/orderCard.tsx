import { Link } from "react-router";
import type OrderDTO from "~/dtos/OrderDTO";

export default function OrderCard({ order }: Readonly<{ order: OrderDTO }>) {

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

  return (
    <div className="order-card">
      <Link to={`/orders/${order.id}`} className="order-card-link">
        <div className="order-information">
          <h3 id={`order-${order.id}-title`}>Pedido #{order.id}-{formatDate(order.createdAt)}-{order.userUsername}</h3>
          <h4 id={`order-${order.id}-user`}>Cliente: {order.userUsername}</h4>
          <h4 id={`order-${order.id}-date`}>Fecha de compra: {formatDate(order.createdAt)}</h4>
          <h4 id={`order-${order.id}-address`}>Dirección de entrega: {order.shippingAddress}</h4>
          <h4 id={`order-${order.id}-status`}>Estado: {order.status}</h4>
          <h4 id={`order-${order.id}-total`}>Total: {order.totalPrice} €</h4>
        </div>
      </Link>
    </div>
  );
}