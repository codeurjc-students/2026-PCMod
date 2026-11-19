import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import { createRoutesStub } from "react-router";
import OrderDetail, { clientLoader } from "~/routes/detail-order";
import { getOrder } from "~/services/orders-service";

const order1 = {
  id: 1,
  status: "FINISHED",
  totalPrice: 121.99,
  shippingAddress: "User address",
  createdAt: "2026/11/19 12:00:00",
  userUsername: "user_example",
  items: [
    {
      id: 1,
      component: {
        id: 1,
        name: "Seagate BarraCuda 3.5",
        description: "Disco duro de 3.5 pulgadas con capacidad de 1TB y velocidad de 7200 RPM.",
        type: "STORAGE",
        brand: "Seagate",
        price: 121.99,
        stock: 10
      },
      componentName: "Seagate BarraCuda 3.5",
      quantity: 1,
      unitPriceAtPurchase: 121.99
    }
  ]
}

vi.mock("~/services/orders-service", () => ({
  getOrder: vi.fn()
}));

describe("OrderDetail", () => {

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders the detail of an order", async () => {

    vi.mocked(getOrder).mockResolvedValueOnce(order1);;

    const RouterStub = createRoutesStub([
      { path: "/orders/1", Component: OrderDetail, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/orders/1"]} />);

    await waitFor(() => {
      expect(screen.getByText(/Pedido #1-/)).toBeInTheDocument();
      expect(screen.getByText("Cliente: user_example")).toBeInTheDocument();
      expect(screen.getByText("Dirección de entrega: User address")).toBeInTheDocument();
      expect(screen.getByText("Precio total del pedido: 121.99 €")).toBeInTheDocument();
      expect(screen.getByText("Componentes del pedido:")).toBeInTheDocument();
      expect(screen.getByText("Seagate BarraCuda 3.5")).toBeInTheDocument();
      expect(screen.getByText("Seagate | STORAGE")).toBeInTheDocument();
      expect(screen.getByText("Cantidad: 1")).toBeInTheDocument();
      expect(screen.getByText("Precio unitario: 121.99 €")).toBeInTheDocument();
      expect(screen.getByText("Precio total del producto: 121.99 €")).toBeInTheDocument();
    });

  });
});