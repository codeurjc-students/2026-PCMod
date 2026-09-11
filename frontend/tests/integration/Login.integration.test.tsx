import { render, screen, waitFor } from "@testing-library/react";
import fetchCookie from "fetch-cookie";
import userEvent from "@testing-library/user-event";
import { createRoutesStub } from "react-router";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import "@testing-library/jest-dom";
import { CookieJar } from "tough-cookie";
import Login from "~/routes/login";

describe("LoginIntegration", () => {

  beforeEach(() => {
    vi.stubGlobal("fetch", fetchCookie(globalThis.fetch, new CookieJar()));
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("renders the login form and validates empty credentials", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    expect(screen.getByRole("heading", { name: "Iniciar sesión:" })).toBeInTheDocument();
    expect(screen.getByLabelText("Correo electrónico:")).toBeInTheDocument();
    expect(screen.getByLabelText("Contraseña:")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    expect(screen.getByText("Por favor, ingrese un correo electrónico válido.")).toBeInTheDocument();
    expect(screen.getByText("Contraseña incorrecta. Inténtelo de nuevo.")).toBeInTheDocument();
  });

  it("logs in with valid credentials and navigates to the home page", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <h1>HomePage</h1> },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "HomePage" })).toBeInTheDocument();
    });
  });

  it("rejects an invalid email format", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    expect(screen.getByText("Por favor, ingrese un correo electrónico válido.")).toBeInTheDocument();
  });

  it("rejects an invalid password format", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "pass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    expect(screen.getByText("Contraseña incorrecta. Inténtelo de nuevo.")).toBeInTheDocument();
  });

  it("shows the backend error when credentials are rejected", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "wrongUser@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "wrongPass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(screen.getByText("Error al iniciar sesión. Por favor, inténtenlo de nuevo")).toBeInTheDocument();
    });
  });

});