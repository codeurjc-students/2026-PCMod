import { render, screen, waitFor } from "@testing-library/react";
import fetchCookie from "fetch-cookie";
import userEvent from "@testing-library/user-event";
import { createRoutesStub, Navigate } from "react-router";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import "@testing-library/jest-dom";
import { CookieJar } from "tough-cookie";
import Login from "~/routes/login";
import Profile, { clientLoader } from "~/routes/profile";
import { register } from "~/services/users-service";
import { useUserStore } from "~/stores/user-store";

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

  it("renders the profile page with the correct data", async () => {
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

});