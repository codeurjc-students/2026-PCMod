import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import { createRoutesStub } from "react-router";
import { useUserStore } from "~/stores/user-store";
import Cart, { clientLoader } from "~/routes/cart";
import { getCart } from "~/services/cart-service";

const component1 = {
  id: 1,
  name: "Seagate BarraCuda 3.5",
  description: "Disco duro interno de 3.5 pulgadas con capacidad de almacenamiento de 1TB y velocidad de rotación de 7200 RPM.",
  type: "STORAGE",
  brand: "Seagate",
  price: 121.99,
  stock: 10
};

const userWithNoCartProductsAccount = {
  id: 1,
  name: "User",
  surname: "NoCartProducts",
  username: "user_example_nocartproducts",
  address: "User address",
  email: "userNoCartProducts@example.com",
  roles: ["REGISTERED_USER"],
  cart: {
    id: 1,
    items: []
  },
  orders:[]
};

const userWithCartProductsAccount = {
  id: 2,
  name: "User",
  surname: "WithCartProducts",
  username: "user_example_withcartproducts",
  address: "User Address WithCartProducts",
  email: "userWithCartProducts@example.com",
  roles: ["REGISTERED_USER"],
  cart: {
    id: 1,
    items: [{ id: 2, component: component1, quantity: 1 }]
  },
  orders:[]
};

vi.mock("~/services/cart-service", () => ({
  getCart: vi.fn(),
}));

describe("Cart", () => {

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders the cart page without products", async () => {

    const RouterStub = createRoutesStub([
      { path: "/cart", Component: Cart, loader: clientLoader },
    ]);

    vi.mocked(getCart).mockResolvedValueOnce({ id: 1, items: [] });

    render(<RouterStub initialEntries={["/cart"]} />);

    await waitFor(() => {
      expect(screen.getByText("No ha añadido productos al carro de compra todavía.")).toBeInTheDocument();
    });

  });

  it("renders the cart page with products", async () => {

    const RouterStub = createRoutesStub([
      { path: "/cart", Component: Cart, loader: clientLoader },
    ]);
    
    vi.mocked(getCart).mockResolvedValueOnce({ id: 1, items: [{ id: 2, component: component1, quantity: 1 }] });

    render(<RouterStub initialEntries={["/cart"]} />);

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

});