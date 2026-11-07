import type UserPublicDTO from "./UserPublicDTO";

export default interface ReviewDTO {
  id: number;
  title: string;
  message: string,
  rating: number,
  date: string,
  componentId: number,
  user: UserPublicDTO
}