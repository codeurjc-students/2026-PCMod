import type ComponentBasicDTO from "./ComponentBasicDTO";

export default interface OrderItemDTO {
  id: number;
  component: ComponentBasicDTO;
  componentName: string;
  quantity: number;
  unitPriceAtPurchase: number;
}