import { render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import { createRoutesStub, MemoryRouter } from "react-router";
import ComponentDetail, { clientLoader } from "~/routes/detail-component";
import { getComponent, deleteComponent, getComponents } from "~/services/components-service";
import { getReviews } from "~/services/reviews-service";
import { reqIsLogged, getPublicUser } from "~/services/users-service";
import { useUserStore } from "~/stores/user-store";
import userEvent from "@testing-library/user-event";
import Components from "~/routes/components";

vi.mock("~/services/components-service", () => ({
  getComponent: vi.fn(),
  deleteComponent: vi.fn(),
}));

vi.mock("~/services/reviews-service", () => ({
  getReviews: vi.fn(),
}));

const userPublic1 = {
  id: 1,
  username: "user_example"
}

const review1 = {
  id: 1,
  title: "sample_title",
  message: "sample_message",
  rating: 5,
  date: "2026-11-07",
  componentId: 2,
  user: userPublic1
}

const component1 = {
  id: 1,
  name: "AMD Ryzen 7 7800X3D",
  description: "Procesador gaming de alto rendimiento.",
  type: "CPU",
  brand: "AMD",
  price: 350,
  stock: 10,
  averageRating: 0,
  reviews: [],
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
  averageRating: 5,
  reviews: [review1],
  image: null,
};

const component3 = {
  id: 3,
  name: "NVIDIA GeForce RTX 4090",
  description: "Gráfica de alto rendimiento.",
  type: "GPU",
  brand: "NVIDIA",
  price: 1150,
  stock: 0,
  averageRating: 0,
  reviews: [],
  image: null,
};

vi.mock("~/services/users-service", () => ({
  reqIsLogged: vi.fn(),
  getPublicUser: vi.fn()
}));

const userAccount = {
  id: 1,
  name: "User",
  surname: "Example",
  username: "user_example",
  address: "User address",
  email: "user@example.com",
  roles: ["REGISTERED_USER"],
};

const adminAccount = {
  id: 2,
  name: "Admin",
  surname: "Example",
  username: "admin_example",
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

  it("renders the component detail page without reviews", async () => {

    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component1, reviews: { items: [], hasNext:false } })},
    ]);

    render(<RouterStub initialEntries={["/components/1"]} />);

    await waitFor(() => {
      expect(screen.getByText(component1.name)).toBeInTheDocument();
      expect(screen.getByText(component1.description)).toBeInTheDocument();
      expect(screen.getByText(component1.price.toFixed(2).toString()  + " €")).toBeInTheDocument();
      expect(screen.getByRole("heading", { name: /AMD\s*\|\s*CPU/ })).toBeInTheDocument();
      expect(screen.getByText("Reseñas del producto:")).toBeInTheDocument();
      expect(document.getElementById("component-average-rating")).toHaveTextContent("Puntuación media: ☆☆☆☆☆ 0/5 (0 reseña(s))");
      expect(screen.getByText("No hay reseñas de este producto todavía.")).toBeInTheDocument();
      expect(document.getElementById("loadMore")).not.toBeInTheDocument();
    });
    
  });

  it("renders the component detail page with reviews", async () => {

    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component2, reviews: { items: [review1], hasNext:true} })},
    ]);

    render(<RouterStub initialEntries={["/components/2"]} />);

    await waitFor(() => {
      expect(screen.getByText(component2.name)).toBeInTheDocument();
      expect(screen.getByText(component2.description)).toBeInTheDocument();
      expect(screen.getByText(component2.price.toFixed(2).toString()  + " €")).toBeInTheDocument();
      expect(screen.getByRole("heading", { name: /Intel\s*\|\s*CPU/ })).toBeInTheDocument();
      expect(screen.getByText("Reseñas del producto:")).toBeInTheDocument();
      expect(document.getElementById("component-average-rating")).toHaveTextContent("Puntuación media: ★★★★★ 5/5 (1 reseña(s))");
      expect(screen.getByText("user_example")).toBeInTheDocument();
      expect(screen.getByText("2026-11-07")).toBeInTheDocument();
      expect(screen.getByText("sample_title")).toBeInTheDocument();
      expect(screen.getByText("sample_message")).toBeInTheDocument();
      expect(screen.getByText("Valoración del producto: ★★★★★ 5/5")).toBeInTheDocument();
      expect(document.getElementById("loadMore")).toBeInTheDocument();
    });
  });

  it("checks handleLoadMore functionality", async () => {
    const user = userEvent.setup();

    vi.mocked(getReviews).mockResolvedValue({
      items: [review1],
      hasNext: false,
    });

    render(
      <MemoryRouter>
        <ComponentDetail
          loaderData={{
            component: component2,
            reviews: { items: [review1], hasNext:true},
          } as any}
        />
      </MemoryRouter>,
    );

    const loadMoreButton = screen.getByRole("button", { name: /Cargar más reseñas/i });
    await user.click(loadMoreButton);

    await waitFor(() => {
      expect(getReviews).toHaveBeenCalledWith(2, 1);
    });

    expect(screen.queryByRole("button", { name: /Cargar más reseñas/i })).not.toBeInTheDocument();
  });

  it("shows an error when loading more reviews fails", async () => {
    const user = userEvent.setup();

    vi.mocked(getReviews).mockRejectedValueOnce(new Error("Network error"));

    render(
      <MemoryRouter>
        <ComponentDetail
          loaderData={{
            component: component2,
            reviews: { items: [review1], hasNext:true},
          } as any}
        />
      </MemoryRouter>,
    );

    await user.click(screen.getByRole("button", { name: /Cargar más reseñas/i }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent(
        "Hubo un problema al cargar más reseñas. Por favor, pruebe de nuevo.",
      );
    });

    expect(getReviews).toHaveBeenCalledWith(2, 1);
    expect(screen.getByRole("button", { name: /Cargar más reseñas/i })).toBeInTheDocument();
  });

  it("renders the last units warning when stock is low", async () => {
      
    vi.mocked(getComponent).mockResolvedValue(component2);

    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component2, reviews: { items: [], hasNext:false } }) },
    ]);

    render(<RouterStub initialEntries={["/components/2"]} />);

    await waitFor(() => {
      expect(screen.getByText("¡Últimas unidades!")).toBeInTheDocument();
    });

  });

  it("does not render the last units warning when stock is not low", async () => {
    vi.mocked(getComponent).mockResolvedValue(component1);

    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component1, reviews: { items: [], hasNext:false } }) },
    ]);

    render(<RouterStub initialEntries={["/components/1"]} />);

    await waitFor(() => {
      expect(screen.queryByText("¡Últimas unidades!")).not.toBeInTheDocument();
    });

  });

  it("renders no stock warning when stock is 0", async () => {
      
    vi.mocked(getComponent).mockResolvedValue(component3);

    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component3, reviews: { items: [], hasNext:false } }) },
    ]);

    render(<RouterStub initialEntries={["/components/3"]} />);

    await waitFor(() => {
      expect(screen.getByText("¡No hay stock!")).toBeInTheDocument();
      expect(document.getElementById("add-to-cart-button")).toBeDisabled();
    });

  });

  it("shows only the component add to cart button for non-registered users", async () => {
    
    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component1, reviews: { items: [], hasNext:false } })},
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
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component1, reviews: { items: [], hasNext:false } })},
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
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component1, reviews: { items: [], hasNext:false } })},
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

  it("deletes component", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component1, reviews: { items: [], hasNext:false } }) },
      { path: "/components",
        Component: Components,
        loader: async ({ request }) => {
          const url = new URL(request.url);
          const page = Number(url.searchParams.get("page") ?? 0);
          const result = await getComponents(page);
          return { items: result.items, hasNext: result.hasNext };
        }
      },
    ]);
  
    vi.mocked(deleteComponent).mockResolvedValue();
    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);
    useUserStore.setState({ user: adminAccount });
  
    render(<RouterStub initialEntries={["/components/1"]} />);
  
    await waitFor(() => {
      expect(document.getElementById("delete-component-button")).toBeInTheDocument();
    });
    await user.click(document.getElementById("delete-component-button")!);
  
    expect(await screen.findByText("¿Está seguro de que desea borrar este componente?")).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Cancelar/ })).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Eliminar componente/ })).toBeInTheDocument();
  
    await user.click(await screen.findByRole("button", { name: /Eliminar componente/ }));
  
    await waitFor(() => {
      expect(deleteComponent).toHaveBeenCalledWith(component1.id);
      expect(screen.queryByText(component1.name)).not.toBeInTheDocument();
    });
  
  });
  
  it("cancel component deletion", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component1, reviews: { items: [], hasNext:false } }) },
      { path: "/components",
        Component: Components,
        loader: async ({ request }) => {
          const url = new URL(request.url);
          const page = Number(url.searchParams.get("page") ?? 0);
          const result = await getComponents(page);
          return { items: result.items, hasNext: result.hasNext };
        }
      },
    ]);
  
    vi.mocked(deleteComponent).mockResolvedValue();
    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);
    useUserStore.setState({ user: adminAccount });
  
    render(<RouterStub initialEntries={["/components/1"]} />);
  
    await waitFor(() => {
      expect(document.getElementById("delete-component-button")).toBeInTheDocument();
    });
    await user.click(document.getElementById("delete-component-button")!);
  
    expect(await screen.findByText("¿Está seguro de que desea borrar este componente?")).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Cancelar/ })).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Eliminar componente/ })).toBeInTheDocument();
  
    await user.click(await screen.findByRole("button", { name: /Cancelar/ }));
  
    await waitFor(() => {
      expect(deleteComponent).not.toHaveBeenCalled();
      expect(screen.queryByText(component1.name)).toBeInTheDocument();
    });

  });
  
  it("cancel component deletion with the close button", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component1, reviews: { items: [], hasNext:false } }) },
      { path: "/components",
        Component: Components,
        loader: async ({ request }) => {
          const url = new URL(request.url);
          const page = Number(url.searchParams.get("page") ?? 0);
          const result = await getComponents(page);
          return { items: result.items, hasNext: result.hasNext };
        }
      },
    ]);
  
    vi.mocked(deleteComponent).mockResolvedValue();
    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);
    useUserStore.setState({ user: adminAccount });
  
    render(<RouterStub initialEntries={["/components/1"]} />);

    await waitFor(() => {
      expect(document.getElementById("delete-component-button")).toBeInTheDocument();
    });
    await user.click(document.getElementById("delete-component-button")!);

    await user.click(await screen.findByRole("button", { name: "Close" }));

    await waitFor(() => {
      expect(screen.queryByText("¿Está seguro de que desea borrar este componente?")).not.toBeInTheDocument();
      expect(deleteComponent).not.toHaveBeenCalled();
    });
  });

  it("clientLoader returns the component and void reviews", async () => {

    const reviews = {
      items: [],
      hasNext: false,
    };

    vi.mocked(getComponent).mockResolvedValue(component1);
    vi.mocked(getReviews).mockResolvedValue(reviews);

    const result = await clientLoader({ params: { id: "1" } } as Parameters<typeof clientLoader>[0]);

    expect(getComponent).toHaveBeenCalledTimes(1);
    expect(getComponent).toHaveBeenCalledWith(1);
    expect(getReviews).toHaveBeenCalledTimes(1);
    expect(getReviews).toHaveBeenCalledWith(component1.id, 0);
    expect(result).toEqual({ component: component1, reviews });

  });

  it("clientLoader returns the component and reviews", async () => {

    const reviews = {
      items: [review1],
      hasNext: false,
    };

    vi.mocked(getComponent).mockResolvedValue(component2);
    vi.mocked(getReviews).mockResolvedValue(reviews);

    const result = await clientLoader({ params: { id: "2" } } as Parameters<typeof clientLoader>[0]);

    expect(getComponent).toHaveBeenCalledTimes(1);
    expect(getComponent).toHaveBeenCalledWith(2);
    expect(getReviews).toHaveBeenCalledTimes(1);
    expect(getReviews).toHaveBeenCalledWith(component2.id, 0);
    expect(result).toEqual({ component: component2, reviews });

  });

  it("gets public user info", async () => {

    const originalPublicUser = {
      id: 1,
      username: "sample_username"
    }

    vi.mocked(getPublicUser).mockResolvedValue(originalPublicUser);

    const publicUser = await getPublicUser(1);

    expect(publicUser.id).toBe(1);
    expect(publicUser.username).toBe("sample_username");

  });

});