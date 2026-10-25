import { render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import { createRoutesStub } from "react-router";
import ComponentDetail, { clientLoader } from "~/routes/detail-component";
import { getComponent } from "~/services/components-service";
import { reqIsLogged } from "~/services/users-service";
import { useUserStore } from "~/stores/user-store";

vi.mock("~/services/components-service", () => ({
  getComponent: vi.fn(),
}));

const component1 = {
  id: 1,
  name: "AMD Ryzen 7 7800X3D",
  description: "Procesador gaming de alto rendimiento.",
  type: "CPU",
  brand: "AMD",
  price: 350,
  stock: 10,
  image: null,
};

const component2 = {
  id: 2,
  name: "Intel Core i5-12400F",
  description: "Procesador de alto rendimiento para gaming y productividad.",
  type: "CPU",
  brand: "Intel",
  price: 150,
  stock: 2,
  image: null,
};

vi.mock("~/services/users-service", () => ({
  reqIsLogged: vi.fn(),
}));

const userAccount = {
  id: 1,
  name: "User",
  surname: "Example",
  username: "user@example.com",
  address: "User address",
  email: "user@example.com",
  roles: ["REGISTERED_USER"],
};

const adminAccount = {
  id: 2,
  name: "Admin",
  surname: "Example",
  username: "admin@example.com",
  address: "Admin address",
  email: "admin@example.com",
  roles: ["REGISTERED_USER", "ADMIN"],
};

describe("ComponentDetailPage", () => {

  beforeEach(() => {
    vi.clearAllMocks();
    useUserStore.setState({ user: null });
    vi.mocked(reqIsLogged).mockResolvedValue(null as never);
  });

  it("renders the component detail page", async () => {

    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => component1},
    ]);

    render(<RouterStub initialEntries={["/components/1"]} />);

    await waitFor(() => {
      expect(screen.getByText(component1.name)).toBeInTheDocument();
      expect(screen.getByText(component1.description)).toBeInTheDocument();
      expect(screen.getByText(component1.price.toFixed(2).toString()  + " €")).toBeInTheDocument();
      expect(screen.getByRole("heading", { name: /AMD\s*\|\s*CPU/ })).toBeInTheDocument();
      expect(screen.getByText("Reseñas del producto:")).toBeInTheDocument();
      expect(screen.getByText("No hay reseñas de este producto todavía.")).toBeInTheDocument();
    });
    
  });

  it("renders the last units warning when stock is low", async () => {
      
    vi.mocked(getComponent).mockResolvedValue(component2);

    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => component2 },
    ]);

    render(<RouterStub initialEntries={["/components/2"]} />);

    await waitFor(() => {
      expect(screen.getByText("¡Últimas unidades!")).toBeInTheDocument();
    });

  });

  it("does not render the last units warning when stock is not low", async () => {
    vi.mocked(getComponent).mockResolvedValue(component1);

    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => component1 },
    ]);

    render(<RouterStub initialEntries={["/components/1"]} />);

    await waitFor(() => {
      expect(screen.queryByText("¡Últimas unidades!")).not.toBeInTheDocument();
    });

  });

  it("shows only the component add to cart button for non-registered users", async () => {
    
    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => component1},
    ]);

    render(<RouterStub initialEntries={["/components/1"]} />);

    await waitFor(() => {
      expect(document.getElementById("add-to-cart-button")).toBeInTheDocument();
      expect(document.getElementById("edit-component-button")).not.toBeInTheDocument();
      expect(document.getElementById("delete-component-button")).not.toBeInTheDocument();
    });

  });

  it("shows only the component add to cart button for logged-in non-admin users", async () => {
      
    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => component1},
    ]);

    vi.mocked(reqIsLogged).mockResolvedValue(userAccount);
    useUserStore.setState({ user: userAccount });

    render(<RouterStub initialEntries={["/components/1"]} />);

    await waitFor(() => {
      expect(document.getElementById("add-to-cart-button")).toBeInTheDocument();
      expect(document.getElementById("edit-component-button")).not.toBeInTheDocument();
      expect(document.getElementById("delete-component-button")).not.toBeInTheDocument();
    });

  });

  it("shows the component edit and delete buttons for admin users", async () => {
    
    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => component1},
    ]);

    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);
    useUserStore.setState({ user: adminAccount });

    render(<RouterStub initialEntries={["/components/1"]} />);

    await waitFor(() => {
      expect(document.getElementById("add-to-cart-button")).toBeInTheDocument();
      expect(document.getElementById("edit-component-button")).toBeInTheDocument();
      expect(document.getElementById("delete-component-button")).toBeInTheDocument();
    });

  });

  it("clientLoader returns the component", async () => {

    vi.mocked(getComponent).mockResolvedValue(component1);

    const result = await clientLoader({ params: { id: "1" } } as Parameters<typeof clientLoader>[0]);

    expect(getComponent).toHaveBeenCalledTimes(1);
    expect(result).toEqual(component1);

  });

});