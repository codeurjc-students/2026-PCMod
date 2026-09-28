import { render, screen, waitFor } from "@testing-library/react";
import fetchCookie from "fetch-cookie";
import userEvent from "@testing-library/user-event";
import { createRoutesStub } from "react-router";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import "@testing-library/jest-dom";
import { CookieJar } from "tough-cookie";
import Register from "~/routes/register";

describe("RegisterIntegration", () => {

  beforeEach(() => {
    vi.stubGlobal("fetch", fetchCookie(globalThis.fetch, new CookieJar()));
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("renders the register form", async () => {

    const RouterStub = createRoutesStub([
      { path: "/register", Component: Register },
    ]);

    render(<RouterStub initialEntries={["/register"]} />);

    expect(screen.getByRole("heading", { name: "Registro:" })).toBeInTheDocument();
    expect(screen.getByLabelText("Nombre:")).toBeInTheDocument();
    expect(screen.getByLabelText("Apellido:")).toBeInTheDocument();
    expect(screen.getByLabelText("Nombre de usuario:")).toBeInTheDocument();
    expect(screen.getByLabelText("Dirección:")).toBeInTheDocument();
    expect(screen.getByLabelText("Correo electrónico:")).toBeInTheDocument();
    expect(screen.getByLabelText("Contraseña:")).toBeInTheDocument();
    expect(screen.getByLabelText("Imagen de perfil:")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Registrarse/i })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Haga click aquí para iniciar sesión con su cuenta." })).toBeInTheDocument();

  });

  it("registers with valid credentials and navigates to the home page", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/register", Component: Register },
      { path: "/", Component: () => <h1>HomePage</h1> },
    ]);

    render(<RouterStub initialEntries={["/register"]} />);

    const file = new File(["dummy content"], "user.jpg", { type: "image/jpeg" });

    await user.type(screen.getByLabelText("Nombre:"), "user");
    await user.type(screen.getByLabelText("Apellido:"), "test");
    await user.type(screen.getByLabelText("Nombre de usuario:"), "userTest");
    await user.type(screen.getByLabelText("Dirección:"), "c/test");
    await user.type(screen.getByLabelText("Correo electrónico:"), "user@test.com");
    await user.type(screen.getByLabelText("Contraseña:"), "R3gister_Test");

    await user.upload(screen.getByLabelText("Imagen de perfil:"), file);

    const registerButton = screen.getByRole("button", { name: /Registrarse/i });
    await user.click(registerButton);

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "HomePage" })).toBeInTheDocument();
    });

  });

  it("register with void credentials", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/register", Component: Register },
    ]);

    render(<RouterStub initialEntries={["/register"]} />);

    const registerButton = screen.getByRole("button", { name: /Registrarse/i });
    await user.click(registerButton);

    expect(screen.getByText("Por favor, ingrese un nombre válido.")).toBeInTheDocument();
    expect(screen.getByText("Por favor, ingrese un apellido válido.")).toBeInTheDocument();
    expect(screen.getByText("Por favor, ingrese un nombre de usuario válido.")).toBeInTheDocument();
    expect(screen.getByText("Por favor, ingrese una dirección válida.")).toBeInTheDocument();
    expect(screen.getByText("Por favor, ingrese un correo electrónico válido.")).toBeInTheDocument();
    expect(screen.getByText("Por favor, ingrese una contraseña válida.")).toBeInTheDocument();

  });

  it("register with existing credentials", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/register", Component: Register },
    ]);

    render(<RouterStub initialEntries={["/register"]} />);

    await user.type(screen.getByLabelText("Nombre:"), "user");
    await user.type(screen.getByLabelText("Apellido:"), "test");
    await user.type(screen.getByLabelText("Nombre de usuario:"), "user_example");
    await user.type(screen.getByLabelText("Dirección:"), "c/test");
    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "R3gister_Test");

    const registerButton = screen.getByRole("button", { name: /Registrarse/i });
    await user.click(registerButton);

    await waitFor(() => {
      expect(screen.getByText(/El nombre de usuario ya existe\./)).toBeInTheDocument();
      expect(screen.getByText(/El email ya existe\./)).toBeInTheDocument();
    });

  });

  it("register with invalid format credentials", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/register", Component: Register },
    ]);

    render(<RouterStub initialEntries={["/register"]} />);

    await user.type(screen.getByLabelText("Nombre:"), "user");
    await user.type(screen.getByLabelText("Apellido:"), "test");
    await user.type(screen.getByLabelText("Nombre de usuario:"), "userTest");
    await user.type(screen.getByLabelText("Dirección:"), "c/test");
    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example");
    await user.type(screen.getByLabelText("Contraseña:"), "TestPass");

    const registerButton = screen.getByRole("button", { name: /Registrarse/i });
    await user.click(registerButton);

    await waitFor(() => {
      expect(screen.getByText(/El formato del email no es válido/)).toBeInTheDocument();
      expect(screen.getByText(/La contraseña debe tener al menos 8 caracteres/)).toBeInTheDocument();
    });

  });

  it("register with invalid image format", async () => {

    const user = userEvent.setup();
    const file = new File(["dummy image"], "user.txt", { type: "text/plain" });
    const RouterStub = createRoutesStub([
      { path: "/register", Component: Register },
    ]);

    render(<RouterStub initialEntries={["/register"]} />);

    await user.type(screen.getByLabelText("Nombre:"), "user");
    await user.type(screen.getByLabelText("Apellido:"), "test");
    await user.type(screen.getByLabelText("Nombre de usuario:"), "userTest");
    await user.type(screen.getByLabelText("Dirección:"), "c/test");
    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "R3gister_Test");
    await user.upload(screen.getByLabelText("Imagen de perfil:"), file);

    await user.click(screen.getByRole("button", { name: /Registrarse/i }));

    await waitFor(() => {
      expect(
        screen.getByText("Por favor, seleccione un archivo de imagen válido.")
      ).toBeInTheDocument();
    });
  });

});