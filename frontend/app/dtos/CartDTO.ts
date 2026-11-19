import type CartItemDTO from "./CartItemDTO";

export default interface CartDTO {
  id: number;
  items: Array<CartItemDTO>;
}