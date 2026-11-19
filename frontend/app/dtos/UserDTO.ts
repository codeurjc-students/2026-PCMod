import type CartItemDTO from "./CartItemDTO";
import type ImageDTO from "./ImageDTO";
import type OrderDTO from "./OrderDTO";

export default interface UserDTO {
  id: number;
  name: string;
  surname: string;
  username: string;
  address: string;
  email: string;
  password: string;
  image: ImageDTO;
  roles: Array<string>;
  cart: Array<CartItemDTO>;
  orders: Array<OrderDTO>;
}