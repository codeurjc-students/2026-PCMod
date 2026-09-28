import type ImageDTO from "./ImageDTO";

export default interface UserDTO {
  id: number;
  name: string;
  surname: string;
  username: string;
  address: string;
  email: string;
  password: string;
  image: ImageDTO;
  roles: Array<string>
}