import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import Profile, { clientLoader } from "~/routes/profile";
import { createRoutesStub } from "react-router";
import userEvent from "@testing-library/user-event";
import { addImage, deleteUser, getUser, logOut, reqIsLogged, updateUser } from "~/services/users-service";

const userAccount = {
  id: 1,
  name: "User",
  surname: "Example",
  username: "user@example.com",
  address: "User address",
  email: "user@example.com",
  roles: ["REGISTERED_USER"],
};

vi.mock("~/services/users-service", () => ({
  addImage: vi.fn(),
  getUser: vi.fn(),
  logIn: vi.fn(),
  logOut: vi.fn(),
  reqIsLogged: vi.fn(),
  deleteUser: vi.fn(),
  updateUser: vi.fn(),
}));

describe("ProfilePage", () => {

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders the profile page", async () => {

    const RouterStub = createRoutesStub([
      { path: "/me", Component: Profile, loader: () => userAccount },
    ]);

    render(<RouterStub initialEntries={["/me"]} />);

    await waitFor(() => {
      expect(screen.getByText("Nombre:")).toBeInTheDocument();
      expect(screen.getByText("Apellidos:")).toBeInTheDocument();
      expect(screen.getByText("Nombre de usuario:")).toBeInTheDocument();
      expect(screen.getByText("Dirección:")).toBeInTheDocument();
      expect(screen.getByText("Correo electrónico:")).toBeInTheDocument();
      expect(screen.getByRole("button", { name: "Borrar cuenta" })).toBeInTheDocument();
      expect(screen.getByRole("img", { name: "UserImage" })).toBeInTheDocument();
      expect(screen.getByRole("heading", { level: 2, name: "Mis compras:" })).toBeInTheDocument();
    });

  });

  it("clientLoader returns the user", async () => {
    vi.mocked(getUser).mockResolvedValue(userAccount);

    const result = await clientLoader();

    expect(getUser).toHaveBeenCalledTimes(1);
    expect(result).toEqual(userAccount);
  });

  it("deletes user account", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/me", Component: Profile, loader: () => userAccount },
      { path: "/", Component: () => <h1>HomePage</h1> }
    ]);

    vi.mocked(deleteUser).mockResolvedValue();
    vi.mocked(logOut).mockResolvedValue();

    render(<RouterStub initialEntries={["/me"]} />);

    await user.click(await screen.findByRole("button", { name: "Borrar cuenta" }));

    expect(await screen.findByText(
      "¿Está seguro de que desea borrar su cuenta?",
    )).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Cancelar/ })).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Eliminar cuenta/ })).toBeInTheDocument();

    await user.click(await screen.findByRole("button", { name: /Eliminar cuenta/ }));

    await waitFor(() => {
      expect(deleteUser).toHaveBeenCalledWith(userAccount.id);
      expect(logOut).toHaveBeenCalledTimes(1);
      expect(screen.getByRole("heading", { name: "HomePage" })).toBeInTheDocument();
    });

  });

  it("cancel user account deletion", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/me", Component: Profile, loader: () => userAccount }
    ]);

    render(<RouterStub initialEntries={["/me"]} />);

    await user.click(await screen.findByRole("button", { name: "Borrar cuenta" }));

    expect(await screen.findByText(
      "¿Está seguro de que desea borrar su cuenta?",
    )).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Cancelar/ })).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Eliminar cuenta/ })).toBeInTheDocument();

    await user.click(await screen.findByRole("button", { name: /Cancelar/ }));

    await waitFor(() => {
      expect(deleteUser).not.toHaveBeenCalled();
      expect(logOut).not.toHaveBeenCalled();
    });

  });

  it("closes user account deletion modal with the close button", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/me", Component: Profile, loader: () => userAccount }
    ]);

    render(<RouterStub initialEntries={["/me"]} />);

    await user.click(await screen.findByRole("button", { name: "Borrar cuenta" }));

    expect(await screen.findByText(
      "¿Está seguro de que desea borrar su cuenta?",
    )).toBeInTheDocument();

    await user.click(await screen.findByRole("button", { name: "Close" }));

    await waitFor(() => {
      expect(screen.queryByText(
        "¿Está seguro de que desea borrar su cuenta?",
      )).not.toBeInTheDocument();
      expect(deleteUser).not.toHaveBeenCalled();
      expect(logOut).not.toHaveBeenCalled();
    });
  });

  it("updates with valid credentials", async () => {

    const user = userEvent.setup();
    vi.mocked(updateUser).mockResolvedValue({
      name: "updated",
      surname: "user",
      username: "updatedUser",
      address: "c/updated",
      email: "user@updated.com",
    });
    vi.mocked(addImage).mockResolvedValue({} as Awaited<ReturnType<typeof addImage>>);

    const RouterStub = createRoutesStub([
      { path: "/me", Component: Profile, loader: () => userAccount },
    ]);

    render(<RouterStub initialEntries={["/me"]} />);

    const file = new File(["dummy content"], "user.jpg", { type: "image/jpeg" });

    const formButton = await screen.findByRole("button", { name: /Editar perfil/i });
    await user.click(formButton);

    await user.clear(screen.getByLabelText("Nombre:"));
    await user.type(screen.getByLabelText("Nombre:"), "updated");
    await user.clear(screen.getByLabelText("Apellido:"));
    await user.type(screen.getByLabelText("Apellido:"), "user");
    await user.clear(screen.getByLabelText("Nombre de usuario:"));
    await user.type(screen.getByLabelText("Nombre de usuario:"), "updatedUser");
    await user.clear(screen.getByLabelText("Dirección:"));
    await user.type(screen.getByLabelText("Dirección:"), "c/updated");
    await user.clear(screen.getByLabelText("Correo electrónico:"));
    await user.type(screen.getByLabelText("Correo electrónico:"), "user@updated.com");
    await user.type(screen.getByLabelText("Contraseña:"), "Updat3_Test");

    await user.upload(screen.getByLabelText("Imagen de perfil:"), file);

    const updateButton = await screen.findByRole("button", { name: /Guardar cambios/i });
    await user.click(updateButton);

    await waitFor(() => {
      expect(updateUser).toHaveBeenCalledTimes(1);
      expect(screen.getByText("updated")).toBeInTheDocument();
      expect(screen.getByText("user")).toBeInTheDocument();
      expect(screen.getByText("updatedUser")).toBeInTheDocument();
      expect(screen.getByText("c/updated")).toBeInTheDocument();
      expect(screen.getByText("user@updated.com")).toBeInTheDocument();
    });

  });

  it("updates with void credentials", async () => {

    const user = userEvent.setup();

    const RouterStub = createRoutesStub([
      { path: "/me", Component: Profile, loader: () => userAccount },
    ]);

    render(<RouterStub initialEntries={["/me"]} />);

    const formButton = await screen.findByRole("button", { name: /Editar perfil/i });
    await user.click(formButton);

    await user.clear(screen.getByLabelText("Nombre:"));
    await user.clear(screen.getByLabelText("Apellido:"));
    await user.clear(screen.getByLabelText("Nombre de usuario:"));
    await user.clear(screen.getByLabelText("Dirección:"));
    await user.clear(screen.getByLabelText("Correo electrónico:"));

    const updateButton = await screen.findByRole("button", { name: /Guardar cambios/i });
    await user.click(updateButton);

    await waitFor(() => {
      expect(updateUser).not.toHaveBeenCalled();
      expect(screen.getByText("Por favor, ingrese un nombre válido.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese un apellido válido.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese un nombre de usuario válido.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese una dirección válida.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese un correo electrónico válido.")).toBeInTheDocument();
    });

  });

  it("updates with existing credentials", async () => {

    const user = userEvent.setup();

    vi.mocked(updateUser).mockRejectedValue(new Error(
      "Validación fallida:\n• El nombre de usuario ya existe.\n• El email ya existe."
    ));

    const RouterStub = createRoutesStub([
      { path: "/me", Component: Profile, loader: () => userAccount },
    ]);

    render(<RouterStub initialEntries={["/me"]} />);

    const formButton = await screen.findByRole("button", { name: /Editar perfil/i });
    await user.click(formButton);

    await user.clear(screen.getByLabelText("Nombre de usuario:"));
    await user.type(screen.getByLabelText("Nombre de usuario:"), "adminExample");
    await user.clear(screen.getByLabelText("Correo electrónico:"));
    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");

    const updateButton = await screen.findByRole("button", { name: /Guardar cambios/i });
    await user.click(updateButton);

    await waitFor(() => {
      expect(updateUser).toHaveBeenCalledTimes(1);
      expect(screen.getByText(/El nombre de usuario ya existe\./)).toBeInTheDocument();
      expect(screen.getByText(/El email ya existe\./)).toBeInTheDocument();
    });

  });

  it("updates with invalid format credentials", async () => {

    const user = userEvent.setup();

    vi.mocked(updateUser).mockRejectedValue(new Error(
      "Validación fallida:\n• El formato del email no es válido. Debe seguir el formato: ejemplo@dominio.com.\n• La contraseña debe tener al menos 8 caracteres, incluir una mayúscula, una minúscula, un número y un carácter especial (@$!%*?&-_)."
    ));

    const RouterStub = createRoutesStub([
      { path: "/me", Component: Profile, loader: () => userAccount },
    ]);

    render(<RouterStub initialEntries={["/me"]} />);

    const formButton = await screen.findByRole("button", { name: /Editar perfil/i });
    await user.click(formButton);

    await user.clear(screen.getByLabelText("Correo electrónico:"));
    await user.type(screen.getByLabelText("Correo electrónico:"), "user@updated");
    await user.type(screen.getByLabelText("Contraseña:"), "updatePass");

    const updateButton = await screen.findByRole("button", { name: /Guardar cambios/i });
    await user.click(updateButton);

    await waitFor(() => {
      expect(updateUser).toHaveBeenCalledTimes(1);
      expect(screen.getByText(/El formato del email no es válido/)).toBeInTheDocument();
      expect(screen.getByText(/La contraseña debe tener al menos 8 caracteres/)).toBeInTheDocument();
    });

  });

  it("updates with invalid image format", async () => {

    const user = userEvent.setup();

    const file = new File(["dummy image"], "user.txt", { type: "text/plain" });

    const RouterStub = createRoutesStub([
      { path: "/me", Component: Profile, loader: () => userAccount },
    ]);

    render(<RouterStub initialEntries={["/me"]} />);

    const formButton = await screen.findByRole("button", { name: /Editar perfil/i });
    await user.click(formButton);

    await user.upload(screen.getByLabelText("Imagen de perfil:"), file);

    const updateButton = await screen.findByRole("button", { name: /Guardar cambios/i });
    await user.click(updateButton);

    await waitFor(() => {
      expect(addImage).not.toHaveBeenCalled();
      expect(screen.getByText("Por favor, seleccione un archivo de imagen válido.")).toBeInTheDocument();
    });

  });

  it("cancel user account update", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/me", Component: Profile, loader: () => userAccount }
    ]);

    render(<RouterStub initialEntries={["/me"]} />);

    const formButton = await screen.findByRole("button", { name: /Editar perfil/i });
    await user.click(formButton);

    const cancelButton = await screen.findByRole("button", { name: /Cancelar/ });
    await user.click(cancelButton);

    await waitFor(() => {
      expect(updateUser).not.toHaveBeenCalled();
    });

  });

});