import { render, waitFor, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import fetchCookie from "fetch-cookie";
import { createRoutesStub, Navigate } from "react-router";
import { CookieJar } from "tough-cookie";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import "@testing-library/jest-dom";
import Cart, { clientLoader } from "~/routes/cart";
import Login from "~/routes/login";
import { useUserStore } from "~/stores/user-store";

describe("CartIntegration", () => {

  beforeEach(() => {
    vi.stubGlobal("fetch", fetchCookie(globalThis.fetch, new CookieJar()));
    useUserStore.setState({ user: null, loginError: null });
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("renders the cart page with products", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/cart" replace /> },
      { path: "/cart", Component: Cart, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(screen.getByText("Resumen del carro de compra:")).toBeInTheDocument();
      expect(screen.getByText("Seagate BarraCuda 3.5")).toBeInTheDocument();
      expect(screen.getByText("Seagate | STORAGE")).toBeInTheDocument();
      expect(screen.getByText("Precio unitario: 121.99 €/unidad")).toBeInTheDocument();
      expect(screen.getByText("121.99 €")).toBeInTheDocument();
      expect(screen.queryByRole("button", { name: /Finalizar compra/i })).toBeInTheDocument();
      expect(screen.queryByText("No ha añadido productos al carro de compra todavía.")).not.toBeInTheDocument();
    });
  });

  it("renders the cart page without products", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/cart" replace /> },
      { path: "/cart", Component: Cart, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "adminpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(screen.queryByText("No ha añadido productos al carro de compra todavía.")).toBeInTheDocument();
    });
  });
})