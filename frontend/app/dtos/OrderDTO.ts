import type OrderItemDTO from "./OrderItemDTO";

export default interface OrderDTO {
  id: number;
  status: string;
  totalPrice: number;
  shippingAddress: string;
  createdAt: string;
  userUsername: string;
  items: Array<OrderItemDTO>;
}