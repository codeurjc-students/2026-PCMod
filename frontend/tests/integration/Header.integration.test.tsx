import { render, screen, waitFor } from "@testing-library/react";
import fetchCookie from "fetch-cookie";
import userEvent from "@testing-library/user-event";
import { createRoutesStub } from "react-router";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import "@testing-library/jest-dom";
import Header from "~/components/header";
import Login from "~/routes/login";
import { useUserStore } from "~/stores/user-store";
import { CookieJar } from "tough-cookie";

describe("HeaderIntegration", () => {

  beforeEach(() => {
    vi.stubGlobal("fetch", fetchCookie(globalThis.fetch, new CookieJar()));
    useUserStore.setState({ user: null, loginError: null });
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("shows the profile and logout options after logging in as a user", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/", Component: Header },
      { path: "/login", Component: Login },
    ]);

    render(<RouterStub initialEntries={["/login"]} />)

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await user.click(await screen.findByRole("button", { name: "User" }));

    expect(screen.getByText("Mi perfil")).toBeInTheDocument();
    expect(screen.queryByText("Administración")).not.toBeInTheDocument();
    expect(screen.getByText("Cerrar sesión")).toBeInTheDocument();

  });

  it("shows the administration option after logging in as an admin", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/", Component: Header },
      { path: "/login", Component: Login },
    ]);

    render(<RouterStub initialEntries={["/login"]} />)

    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "adminpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await user.click(await screen.findByRole("button", { name: "User" }));

    expect(screen.getByText("Mi perfil")).toBeInTheDocument();
    expect(screen.getByText("Administración")).toBeInTheDocument();
    expect(screen.getByText("Cerrar sesión")).toBeInTheDocument();

  });

  it("closes the user session from the dropdown", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/", Component: Header },
      { path: "/login", Component: Login },
    ]);

    render(<RouterStub initialEntries={["/login"]} />)

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await user.click(await screen.findByRole("button", { name: "User" }));
    await user.click(screen.getByText("Cerrar sesión"));

    await waitFor(() => {
      expect(screen.getByRole("link", { name: /Iniciar Sesión/i })).toBeInTheDocument();
    });
    expect(screen.queryByRole("button", { name: "User" })).not.toBeInTheDocument();

  });

});
