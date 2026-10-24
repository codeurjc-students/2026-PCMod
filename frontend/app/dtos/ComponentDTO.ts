import type ImageDTO from "./ImageDTO";

export default interface ComponentDTO {
  id: number;
  name: string;
  description: string;
  type: string;
  brand: string;
  price: number;
  stock: number;
  image: ImageDTO
}