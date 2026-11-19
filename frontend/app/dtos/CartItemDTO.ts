import type ComponentBasicDTO from "./ComponentBasicDTO";

export default interface CartItemDTO {
  id: number;
  component: ComponentBasicDTO;
  quantity: number;
}