import { render, screen, waitFor } from "@testing-library/react";
import fetchCookie from "fetch-cookie";
import userEvent from "@testing-library/user-event";
import { createRoutesStub, Navigate } from "react-router";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import "@testing-library/jest-dom";
import { CookieJar } from "tough-cookie";
import Login from "~/routes/login";
import Profile, { clientLoader } from "~/routes/profile";
import { addImage, register } from "~/services/users-service";
import { useUserStore } from "~/stores/user-store";
import type ImageDTO from "~/dtos/ImageDTO";
import Register from "~/routes/register";

function RootRoute() {
  const user = useUserStore((state) => state.user);

  return user ? <Navigate to="/me" replace /> : <h1>HomePage</h1>;
}

describe("ProfileIntegration", () => {

  beforeEach(() => {
    vi.stubGlobal("fetch", fetchCookie(globalThis.fetch, new CookieJar()));
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("renders the profile page with the correct data and purchases", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/me" replace /> },
      { path: "/me", Component: Profile, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(screen.getByText("user")).toBeInTheDocument();
      expect(screen.getByText("example")).toBeInTheDocument();
      expect(screen.getByText("user_example")).toBeInTheDocument();
      expect(screen.getByText("c/example_address 1")).toBeInTheDocument();
      expect(screen.getByText("user@example.com")).toBeInTheDocument();
      expect(screen.getByRole("heading", { name: "Mis compras:" })).toBeInTheDocument();
      expect(screen.queryByText("No se han realizado compras todavía.")).not.toBeInTheDocument();
      expect(screen.getByText(/Pedido #1-/)).toBeInTheDocument();
      expect(screen.getByText("Cliente: user_example")).toBeInTheDocument();
      expect(screen.getByText("Dirección de entrega: c/example_address 1")).toBeInTheDocument();
      expect(screen.getByText("Estado: FINISHED")).toBeInTheDocument();
      expect(screen.getByText("Total: 121.99 €")).toBeInTheDocument();
    });
  });

  it("renders the profile page with the correct data and no purchases", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/register", Component: Register },
      { path: "/", Component: () => <Navigate to="/me" replace /> },
      { path: "/me", Component: Profile, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/register"]} />);

    await user.type(screen.getByLabelText("Nombre:"), "user");
    await user.type(screen.getByLabelText("Apellido:"), "test");
    await user.type(screen.getByLabelText("Nombre de usuario:"), "userTestNoPurchases");
    await user.type(screen.getByLabelText("Dirección:"), "c/test");
    await user.type(screen.getByLabelText("Correo electrónico:"), "userNoPurchases@test.com");
    await user.type(screen.getByLabelText("Contraseña:"), "R3gister_Test");

    const registerButton = screen.getByRole("button", { name: /Registrarse/i });
    await user.click(registerButton);

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "Mis compras:" })).toBeInTheDocument();
      expect(screen.getByText("No se han realizado compras todavía.")).toBeInTheDocument();
    });
  });

  it("cancels deleting the user account", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/me" replace /> },
      { path: "/me", Component: Profile, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await user.click(await screen.findByRole("button", { name: "Borrar cuenta" }));
    await user.click(await screen.findByRole("button", { name: /Cancelar/ }));

    await waitFor(() => {
      expect(screen.queryByText("¿Está seguro de que desea borrar su cuenta?")).not.toBeInTheDocument();
      expect(screen.getByRole("button", { name: "Borrar cuenta" })).toBeInTheDocument();
      expect(screen.getByText("user@example.com")).toBeInTheDocument();
    });
  });

  it("cancels deleting the user account closing the modal", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/me" replace /> },
      { path: "/me", Component: Profile, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await user.click(await screen.findByRole("button", { name: "Borrar cuenta" }));
    await user.click(await screen.findByRole("button", { name: "Close" }));

    await waitFor(() => {
      expect(screen.queryByText("¿Está seguro de que desea borrar su cuenta?")).not.toBeInTheDocument();
      expect(screen.getByRole("button", { name: "Borrar cuenta" })).toBeInTheDocument();
      expect(screen.getByText("user@example.com")).toBeInTheDocument();
    });
  });

  it("deletes user account", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: RootRoute },
      { path: "/me", Component: Profile, loader: clientLoader },
    ]);

    await register(
      "delete",
      "account",
      "delete_account",
      "c/delete_account_address 1",
      "delete.account@example.com",
      "delete_Pass1",
    );

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "delete.account@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "delete_Pass1");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await user.click(await screen.findByRole("button", { name: "Borrar cuenta" }));

    expect(await screen.findByText(
      "¿Está seguro de que desea borrar su cuenta?",
    )).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Cancelar/ })).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Eliminar cuenta/ })).toBeInTheDocument();

    await user.click(await screen.findByRole("button", { name: /Eliminar cuenta/ }));

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "HomePage" })).toBeInTheDocument();
    });
  });

  it("updates with valid credentials", async () => {

    const user = userEvent.setup();
    await register(
      "update",
      "account",
      "update_account",
      "c/update_account_address 1",
      "update.account@example.com",
      "update_Pass1",
    );

    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/me" replace /> },
      { path: "/me", Component: Profile, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "update.account@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "update_Pass1");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

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
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/me" replace /> },
      { path: "/me", Component: Profile, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    const formButton = await screen.findByRole("button", { name: /Editar perfil/i });
    await user.click(formButton);

    await user.clear(screen.getByLabelText("Nombre:"));
    await user.clear(screen.getByLabelText("Apellido:"));
    await user.clear(screen.getByLabelText("Nombre de usuario:"));
    await user.clear(screen.getByLabelText("Dirección:"));
    await user.clear(screen.getByLabelText("Correo electrónico:"));

    const updateButton = await screen.findByRole("button", { name: /Guardar cambios/i });
    await user.click(updateButton);

    expect(screen.getByText("Por favor, ingrese un nombre válido.")).toBeInTheDocument();
    expect(screen.getByText("Por favor, ingrese un apellido válido.")).toBeInTheDocument();
    expect(screen.getByText("Por favor, ingrese un nombre de usuario válido.")).toBeInTheDocument();
    expect(screen.getByText("Por favor, ingrese una dirección válida.")).toBeInTheDocument();
    expect(screen.getByText("Por favor, ingrese un correo electrónico válido.")).toBeInTheDocument();

  });

  it("updates with existing credentials", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/me" replace /> },
      { path: "/me", Component: Profile, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    const formButton = await screen.findByRole("button", { name: /Editar perfil/i });
    await user.click(formButton);

    await user.clear(screen.getByLabelText("Nombre de usuario:"));
    await user.type(screen.getByLabelText("Nombre de usuario:"), "admin_example");
    await user.clear(screen.getByLabelText("Correo electrónico:"));
    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");

    const updateButton = await screen.findByRole("button", { name: /Guardar cambios/i });
    await user.click(updateButton);

    await waitFor(() => {
      expect(screen.getByText(/El nombre de usuario ya existe\./)).toBeInTheDocument();
      expect(screen.getByText(/El email ya existe\./)).toBeInTheDocument();
    });

  });

  it("updates with invalid format credentials", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/me" replace /> },
      { path: "/me", Component: Profile, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    const formButton = await screen.findByRole("button", { name: /Editar perfil/i });
    await user.click(formButton);

    await user.clear(screen.getByLabelText("Correo electrónico:"));
    await user.type(screen.getByLabelText("Correo electrónico:"), "user@updated");
    await user.type(screen.getByLabelText("Contraseña:"), "updatePass");

    const updateButton = await screen.findByRole("button", { name: /Guardar cambios/i });
    await user.click(updateButton);
    await waitFor(() => {
      expect(screen.getByText(/El formato del email no es válido/)).toBeInTheDocument();
      expect(screen.getByText(/La contraseña debe tener al menos 8 caracteres/)).toBeInTheDocument();
    });

  });

  it("updates with invalid image format", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/me" replace /> },
      { path: "/me", Component: Profile, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    const file = new File(["dummy image"], "user.txt", { type: "text/plain" });

    const formButton = await screen.findByRole("button", { name: /Editar perfil/i });
    await user.click(formButton);

    await user.upload(screen.getByLabelText("Imagen de perfil:"), file);

    const updateButton = await screen.findByRole("button", { name: /Guardar cambios/i });
    await user.click(updateButton);

    await waitFor(() => {
      expect(screen.getByText("Por favor, seleccione un archivo de imagen válido.")).toBeInTheDocument();
    });

  });

  it("uploads an image successfully", async () => {

    const image: ImageDTO = { id: 1 };

    const file = new File(["dummy image content"], "test.jpg", { type: "image/jpeg" });

    vi.spyOn(globalThis, "fetch").mockResolvedValueOnce(
      new Response(
        JSON.stringify(image),
        {
          status: 200,
          headers: { "Content-Type": "application/json" }
        }
      )
    );

    const result = await addImage(1, file);

    expect(result).toEqual(image);
  });

  it("throws an error when uploading an image fails", async () => {

    const file = new File(["dummy image content"], "test.jpg", { type: "image/jpeg" });

    vi.spyOn(globalThis, "fetch").mockResolvedValueOnce(
      new Response(
        JSON.stringify({ }),
        { status: 400 }
      )
    );

    await expect(addImage(1, file)).rejects.toThrow(
      "Hubo un error al subir la imagen. Solo se permiten imágenes JPEG, PNG y WebP."
    );
  });

  it("throws an error when the updateUser request fails with a backend message", async () => {
    const user = userEvent.setup();
    const backendErrorMessage = "sample_backend_error";
    await register(
      "updateError",
      "accountError",
      "update_account_error",
      "c/update_account_address 1",
      "update.error@example.com",
      "update_Pass1",
    );

    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/me" replace /> },
      { path: "/me", Component: Profile, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "update.error@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "update_Pass1");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    const formButton = await screen.findByRole("button", { name: /Editar perfil/i });
    await user.click(formButton);

    await user.clear(screen.getByLabelText("Nombre:"));
    await user.type(screen.getByLabelText("Nombre:"), "updated");
    await user.clear(screen.getByLabelText("Apellido:"));
    await user.type(screen.getByLabelText("Apellido:"), "user");
    await user.clear(screen.getByLabelText("Nombre de usuario:"));
    await user.type(screen.getByLabelText("Nombre de usuario:"), "updatedUserError");
    await user.clear(screen.getByLabelText("Dirección:"));
    await user.type(screen.getByLabelText("Dirección:"), "c/updated");
    await user.clear(screen.getByLabelText("Correo electrónico:"));
    await user.type(screen.getByLabelText("Correo electrónico:"), "user@error.com");
    await user.type(screen.getByLabelText("Contraseña:"), "Updat3_Test");

    vi.spyOn(globalThis, "fetch").mockImplementationOnce(() => Promise.resolve(
      new Response(
        JSON.stringify({ message: backendErrorMessage }),
        { status: 400, headers: { "Content-Type": "application/json" } },
      ),
    ));

    const updateButton = await screen.findByRole("button", { name: /Guardar cambios/i });
    await user.click(updateButton);

    await waitFor(() => {
      expect(screen.getByText(backendErrorMessage)).toBeInTheDocument();
    });
  });

  it("throws an error when the updateComponent request fails without a backend message", async () => {
    const user = userEvent.setup();
    await register(
      "updateError2",
      "accountError2",
      "update_account_error2",
      "c/update_account_address 1",
      "update.error2@example.com",
      "update_Pass1",
    );

    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/me" replace /> },
      { path: "/me", Component: Profile, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "update.error@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "update_Pass1");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    const formButton = await screen.findByRole("button", { name: /Editar perfil/i });
    await user.click(formButton);

    await user.clear(screen.getByLabelText("Nombre:"));
    await user.type(screen.getByLabelText("Nombre:"), "updated");
    await user.clear(screen.getByLabelText("Apellido:"));
    await user.type(screen.getByLabelText("Apellido:"), "user");
    await user.clear(screen.getByLabelText("Nombre de usuario:"));
    await user.type(screen.getByLabelText("Nombre de usuario:"), "updatedUserError");
    await user.clear(screen.getByLabelText("Dirección:"));
    await user.type(screen.getByLabelText("Dirección:"), "c/updated");
    await user.clear(screen.getByLabelText("Correo electrónico:"));
    await user.type(screen.getByLabelText("Correo electrónico:"), "user@error.com");
    await user.type(screen.getByLabelText("Contraseña:"), "Updat3_Test");

    vi.spyOn(globalThis, "fetch").mockImplementationOnce(() => Promise.resolve(
      new Response(
        JSON.stringify({ }),
        { status: 400, headers: { "Content-Type": "application/json" } },
      ),
    ));

    const updateButton = await screen.findByRole("button", { name: /Guardar cambios/i });
    await user.click(updateButton);

    await waitFor(() => {
      expect(screen.getByText("Hubo un error al actualizar el perfil.")).toBeInTheDocument();
    });
  });

});