import { create } from "zustand";

import type UserDTO from "~/dtos/UserDTO";
import { HttpError, logIn, logOut, reqIsLogged } from "~/services/login-service";

interface UserState {
  user: UserDTO | null;
  loginError: string | null;
  loadLoggedUser: () => Promise<void>;
  loginUser: (username: string, password: string) => Promise<void>;
  logoutUser: () => Promise<void>;
}

export const useUserStore = create<UserState>((set, get) => ({

  user: null,
  loginError: null,

  loadLoggedUser: async () => {

    set({ user: null, loginError: null });

    try {

      const user = await reqIsLogged();
      set({ user });

    } catch (error) {

      if (error instanceof HttpError && error.status === 401) {
        set({ user: null, loginError: null });
        return;
      }

      console.log(error);
      set({ loginError: "Error al cargar el usuario" });
    }

  },

  loginUser: async (username: string, password: string) => {

    set({ user: null, loginError: null });

    try {

      await logIn(username, password);
      await get().loadLoggedUser();

    } catch (error) {

      console.log(error);
      const message = "Error al iniciar sesión. Por favor, inténtenlo de nuevo";
      set({ loginError: message });

    }

  },

  logoutUser: async () => {

    set({ user: null, loginError: null });

    try {

      await logOut();

    } catch (error) {

      console.log(error);
      set({ loginError: "Error al cerrar sesión. Por favor, inténtenlo de nuevo" });

    }

  },

}));
