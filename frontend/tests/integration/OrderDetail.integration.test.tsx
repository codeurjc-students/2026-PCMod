import { render, waitFor, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import fetchCookie from "fetch-cookie";
import { createRoutesStub, Navigate } from "react-router";
import { CookieJar } from "tough-cookie";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import "@testing-library/jest-dom";
import OrderDetail, { clientLoader } from "~/routes/detail-order";
import Login from "~/routes/login";
import { useUserStore } from "~/stores/user-store";

describe("OrderDetailIntegration", () => {

  beforeEach(() => {
    vi.stubGlobal("fetch", fetchCookie(globalThis.fetch, new CookieJar()));
    useUserStore.setState({ user: null, loginError: null });
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("renders the detail order page", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/orders/1" replace /> },
      { path: "/orders/:id", Component: OrderDetail, loader: clientLoader }
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(screen.getByText(/Pedido #1-/)).toBeInTheDocument();
      expect(screen.getByText("Cliente: user_example")).toBeInTheDocument();
      expect(screen.getByText("Dirección de entrega: c/example_address 1")).toBeInTheDocument();
      expect(screen.getByText("Precio total del pedido: 121.99 €")).toBeInTheDocument();
      expect(screen.getByText("Componentes del pedido:")).toBeInTheDocument();
      expect(screen.getByText("Seagate BarraCuda 3.5")).toBeInTheDocument();
      expect(screen.getByText("Seagate | STORAGE")).toBeInTheDocument();
      expect(screen.getByText("Cantidad: 1")).toBeInTheDocument();
      expect(screen.getByText("Precio unitario: 121.99 €")).toBeInTheDocument();
      expect(screen.getByText("Precio total del producto: 121.99 €")).toBeInTheDocument();
    });
  });
})