import type UserDTO from "~/dtos/UserDTO";
import { HttpError, reqIsLogged } from "~/services/users-service";

export async function requireAdminUser(): Promise<UserDTO> {

    const user = await reqIsLogged();

    if (!user) {
      throw new HttpError(401, "Unauthorized");
    }

    const isAdmin = user.roles?.includes("ADMIN");

    if (!isAdmin) {
      throw new HttpError(403, "Forbidden");
    }

    return user;
}
