import type ImageDTO from "./ImageDTO";
import type ReviewDTO from "./ReviewDTO";

export default interface ComponentDTO {
  id: number;
  name: string;
  description: string;
  type: string;
  brand: string;
  price: number;
  stock: number;
  averageRating: number;
  reviews: ReviewDTO[];
  image: ImageDTO
}