import { render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import { createMemoryRouter, createRoutesStub, RouterProvider } from "react-router";
import ComponentDetail, { clientLoader } from "~/routes/detail-component";
import { getComponent, deleteComponent, getComponents } from "~/services/components-service";
import { createReview, deleteReview, getReviews, hasUserReviewed } from "~/services/reviews-service";
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
  createReview: vi.fn(),
  deleteReview: vi.fn(),
  hasUserReviewed: vi.fn(),
}));

const userPublic1 = {
  id: 1,
  username: "user_example"
}

const adminPublic1 = {
  id: 2,
  username: "admin_example"
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

const review2 = {
  id: 2,
  title: "sample_title2",
  message: "sample_message2",
  rating: 5,
  date: "2026-11-07",
  componentId: 2,
  user: adminPublic1
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
    vi.unstubAllGlobals();
    useUserStore.setState({ user: null });
    vi.mocked(reqIsLogged).mockResolvedValue(null as never);
  });

  it("renders the component detail page without reviews", async () => {

    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component1, reviews: { items: [], hasNext: false } }) },
    ]);

    render(<RouterStub initialEntries={["/components/1"]} />);

    await waitFor(() => {
      expect(screen.getByText(component1.name)).toBeInTheDocument();
      expect(screen.getByText(component1.description)).toBeInTheDocument();
      expect(screen.getByText(component1.price.toFixed(2).toString() + " €")).toBeInTheDocument();
      expect(screen.getByRole("heading", { name: /AMD\s*\|\s*CPU/ })).toBeInTheDocument();
      expect(screen.getByText("Reseñas del producto:")).toBeInTheDocument();
      expect(document.getElementById("component-average-rating")).toHaveTextContent("Puntuación media: ☆☆☆☆☆ 0/5 (0 reseña(s))");
      expect(screen.getByText("No hay reseñas de este producto todavía.")).toBeInTheDocument();
      expect(document.getElementById("loadMore")).not.toBeInTheDocument();
    });
    
  });

  it("renders the component detail page with reviews", async () => {

    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component2, reviews: { items: [review1], hasNext: true } }) },
    ]);

    render(<RouterStub initialEntries={["/components/2"]} />);

    await waitFor(() => {
      expect(screen.getByText(component2.name)).toBeInTheDocument();
      expect(screen.getByText(component2.description)).toBeInTheDocument();
      expect(screen.getByText(component2.price.toFixed(2).toString() + " €")).toBeInTheDocument();
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

    const router = createMemoryRouter(
      [{
          path: "/components/:id",
          element: (
            <ComponentDetail
              loaderData={{
                component: component2,
                reviews: {
                  items: [review1],
                  hasNext: true,
                },
              } as any}
            />
          ),
        },
      ],
      {
        initialEntries: ["/components/2"],
      },
    );

    render(<RouterProvider router={router} />);

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

    const router = createMemoryRouter(
      [{
          path: "/components/:id",
          element: (
            <ComponentDetail
              loaderData={{
                component: component2,
                reviews: {
                  items: [review1],
                  hasNext: true,
                },
              } as any}
            />
          ),
        },
      ],
      {
        initialEntries: ["/components/2"],
      },
    );

    render(<RouterProvider router={router} />);

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
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component2, reviews: { items: [], hasNext: false } }) },
    ]);

    render(<RouterStub initialEntries={["/components/2"]} />);

    await waitFor(() => {
      expect(screen.getByText("¡Últimas unidades!")).toBeInTheDocument();
    });

  });

  it("does not render the last units warning when stock is not low", async () => {
    vi.mocked(getComponent).mockResolvedValue(component1);

    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component1, reviews: { items: [], hasNext: false } }) },
    ]);

    render(<RouterStub initialEntries={["/components/1"]} />);

    await waitFor(() => {
      expect(screen.queryByText("¡Últimas unidades!")).not.toBeInTheDocument();
    });

  });

  it("renders no stock warning when stock is 0", async () => {
      
    vi.mocked(getComponent).mockResolvedValue(component3);

    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component3, reviews: { items: [], hasNext: false } }) },
    ]);

    render(<RouterStub initialEntries={["/components/3"]} />);

    await waitFor(() => {
      expect(screen.getByText("¡No hay stock!")).toBeInTheDocument();
      expect(document.getElementById("add-to-cart-button")).toBeDisabled();
    });

  });

  it("shows only the component add to cart button for non-registered users", async () => {
    
    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component1, reviews: { items: [], hasNext: false } }) },
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
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component1, reviews: { items: [], hasNext: false } }) },
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
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component1, reviews: { items: [], hasNext: false } }) },
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
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component1, reviews: { items: [], hasNext: false } }) },
      {
        path: "/components",
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
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component1, reviews: { items: [], hasNext: false } }) },
      {
        path: "/components",
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
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component1, reviews: { items: [], hasNext: false } }) },
      {
        path: "/components",
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
    vi.mocked(hasUserReviewed).mockResolvedValue(false);

    const result = await clientLoader({ params: { id: "1" } } as Parameters<typeof clientLoader>[0]);

    expect(getComponent).toHaveBeenCalledTimes(1);
    expect(getComponent).toHaveBeenCalledWith(1);
    expect(getReviews).toHaveBeenCalledTimes(1);
    expect(getReviews).toHaveBeenCalledWith(component1.id, 0);
    expect(hasUserReviewed).toHaveBeenCalledTimes(1);
    expect(result).toEqual({ component: component1, reviews, hasReviewed: false});

  });

  it("clientLoader returns the component and reviews", async () => {

    const reviews = {
      items: [review1],
      hasNext: false,
    };

    vi.mocked(getComponent).mockResolvedValue(component2);
    vi.mocked(getReviews).mockResolvedValue(reviews);
    vi.mocked(hasUserReviewed).mockResolvedValue(true);

    const result = await clientLoader({ params: { id: "2" } } as Parameters<typeof clientLoader>[0]);

    expect(getComponent).toHaveBeenCalledTimes(1);
    expect(getComponent).toHaveBeenCalledWith(2);
    expect(getReviews).toHaveBeenCalledTimes(1);
    expect(hasUserReviewed).toHaveBeenCalledTimes(1);
    expect(getReviews).toHaveBeenCalledWith(component2.id, 0);
    expect(result).toEqual({ component: component2, reviews, hasReviewed: true });

  });

  it("clientLoader throws the backend error when checking user review availability fails", async () => {
    const backendErrorMessage = "sample_backend_error";
    const reviews = {
      items: [],
      hasNext: false,
    };

    vi.mocked(getComponent).mockResolvedValue(component1);
    vi.mocked(getReviews).mockResolvedValue(reviews);
    vi.mocked(hasUserReviewed).mockRejectedValue(new Error(backendErrorMessage));

    await expect(
      clientLoader({ params: { id: "1" } } as Parameters<typeof clientLoader>[0]),
    ).rejects.toThrow(backendErrorMessage);

  });

  it("clientLoader throws the default error when checking user review availability fails without a message", async () => {
    const reviews = {
      items: [],
      hasNext: false,
    };

    vi.mocked(getComponent).mockResolvedValue(component1);
    vi.mocked(getReviews).mockResolvedValue(reviews);
    vi.mocked(hasUserReviewed).mockRejectedValue(
      new Error("There was an error while checking user review availability."),
    );

    await expect(
      clientLoader({ params: { id: "1" } } as Parameters<typeof clientLoader>[0]),
    ).rejects.toThrow("There was an error while checking user review availability.");

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

  it("does not render the review form when user is not logged in", async () => {
    vi.mocked(getComponent).mockResolvedValue(component1);

    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component1, reviews: { items: [], hasNext: false } }) },
    ]);

    render(<RouterStub initialEntries={["/components/1"]} />);

    await waitFor(() => {
      expect(screen.queryByText("¡Añade una reseña a este producto!")).not.toBeInTheDocument();
    });

  });

  it("does not render the review form when user has already reviewed the component", async () => {

    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component2, reviews: { items: [review1, review2], hasNext: false } }) },
    ]);

    vi.mocked(reqIsLogged).mockResolvedValue(userAccount);
    useUserStore.setState({ user: userAccount });

    vi.mocked(hasUserReviewed).mockResolvedValue(true);

    render(<RouterStub initialEntries={["/components/2"]} />);

    await waitFor(() => {
      expect(screen.queryByText("¡Añade una reseña a este producto!")).not.toBeInTheDocument();
    });

  });

  it("renders the review form when user has not reviewed the component", async () => {

    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component1, reviews: { items: [], hasNext: false } }) },
    ]);

    vi.mocked(reqIsLogged).mockResolvedValue(userAccount);
    useUserStore.setState({ user: userAccount });

    vi.mocked(hasUserReviewed).mockResolvedValue(false);

    render(<RouterStub initialEntries={["/components/1"]} />);

    await waitFor(() => {
      expect(screen.queryByText("¡Añade una reseña a este producto!")).toBeInTheDocument();
      expect(screen.getByLabelText("Título:")).toBeInTheDocument();
      expect(screen.getByLabelText("Mensaje:")).toBeInTheDocument();
      expect(screen.getByLabelText("Valoración:")).toBeInTheDocument();
    });

  });

  it("creates a new review with valid data", async () => {

    const user = userEvent.setup();

    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);
    useUserStore.setState({ user: adminAccount });

    vi.mocked(hasUserReviewed).mockResolvedValue(false);
    vi.mocked(createReview).mockImplementation(async () => {
      reviewCreated = true;

      return {
        id: 3,
        title: "sample_title",
        message: "sample_message",
        rating: 5,
        date: "2026-11-07",
        componentId: 1,
        user: adminPublic1
      };
    });

    let reviewCreated = false;

    const RouterStub = createRoutesStub([
      {
        path: "/components/:id",
        Component: ComponentDetail,
        loader: () => ({
          component: reviewCreated
            ? {
                ...component1,
                averageRating: 5,
                reviews: [{ id: 3 }]
              }
            : component1,
          reviews: {
            items: reviewCreated
              ? [{
                  id: 3,
                  title: "sample_title",
                  message: "sample_message",
                  rating: 5,
                  date: "2026-11-07",
                  componentId: 1,
                  user: adminPublic1
                }]
              : [],
            hasNext: false
          },
          hasReviewed: reviewCreated
        })
      }
    ]);

    render(<RouterStub initialEntries={["/components/1"]} />);

    await screen.findByText("AMD Ryzen 7 7800X3D");
    await user.type(screen.getByLabelText("Título:"), "sample_title");
    await user.type(screen.getByLabelText("Mensaje:"), "sample_message");
    await user.type(screen.getByLabelText("Valoración:"), "5");

    const createButton = await screen.findByRole("button", { name: /Guardar reseña/i });
    await user.click(createButton);

    await waitFor(() => {
      expect(createReview).toHaveBeenCalledTimes(1);
      expect(screen.getByText(/5\/5 \(1 reseña\(s\)\)/i)).toBeInTheDocument();
      expect(screen.getByText("sample_title")).toBeInTheDocument();
      expect(screen.getByText("sample_message")).toBeInTheDocument();
      expect(screen.getByText("Valoración del producto: ★★★★★ 5/5")).toBeInTheDocument();
    });

  });

  it("show error when creating a new review with void data", async () => {

    const user = userEvent.setup();
    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);
    useUserStore.setState({ user: adminAccount });

    vi.mocked(hasUserReviewed).mockResolvedValue(false);

    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component1, reviews: { items: [], hasNext: false }, hasReviewed: false }) },
    ]);

    render(<RouterStub initialEntries={["/components/1"]} />);

    const createButton = await screen.findByRole("button", { name: /Guardar reseña/i });
    await user.click(createButton);

    await waitFor(() => {
      expect(createReview).not.toHaveBeenCalled();
      expect(screen.getByText("Por favor, ingrese un título válido.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese un mensaje válido.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese una valoración válida.")).toBeInTheDocument();
    });

  });

  it("show error when creating a new review with invalid data", async () => {

    const user = userEvent.setup();
    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);
    useUserStore.setState({ user: adminAccount });

    vi.mocked(hasUserReviewed).mockResolvedValue(false);

    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component1, reviews: { items: [], hasNext: false }, hasReviewed: false }) },
    ]);

    render(<RouterStub initialEntries={["/components/1"]} />);

    await screen.findByText("AMD Ryzen 7 7800X3D");
    await user.type(screen.getByLabelText("Título:"), "sample_title");
    await user.type(screen.getByLabelText("Mensaje:"), "sample_message");
    await user.type(screen.getByLabelText("Valoración:"), "-5");

    const createButton = await screen.findByRole("button", { name: /Guardar reseña/i });
    await user.click(createButton);

    await waitFor(() => {
      expect(createReview).not.toHaveBeenCalled();
      expect(screen.getByText("Por favor, ingrese una valoración válida.")).toBeInTheDocument();
    });

  });

  it("throws an error when the createReview request fails with a backend message", async () => {
    const user = userEvent.setup();
    const backendErrorMessage = "sample_error_message";

    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);
    useUserStore.setState({ user: adminAccount });
    vi.mocked(hasUserReviewed).mockResolvedValue(false);
    vi.mocked(createReview).mockRejectedValue(new Error(backendErrorMessage));

    const RouterStub = createRoutesStub([
      {
        path: "/components/:id",
        Component: ComponentDetail,
        loader: () => ({
          component: component1,
          reviews: { items: [], hasNext: false },
          hasReviewed: false,
        }),
      },
    ]);

    render(<RouterStub initialEntries={["/components/1"]} />);

    await screen.findByText("AMD Ryzen 7 7800X3D");
    await user.type(screen.getByLabelText("Título:"), "sample_title");
    await user.type(screen.getByLabelText("Mensaje:"), "sample_message");
    await user.type(screen.getByLabelText("Valoración:"), "5");
    await user.click(screen.getByRole("button", { name: /Guardar reseña/i }));

    await waitFor(() => {
      expect(createReview).toHaveBeenCalledWith(1, "sample_title", "sample_message", 5);
      expect(screen.getByText(backendErrorMessage)).toBeInTheDocument();
      expect(screen.getByText("¡Añade una reseña a este producto!")).toBeInTheDocument();
    });

  });

  it("throws the default error when the createReview request fails with a backend message", async () => {
    const user = userEvent.setup();

    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);
    useUserStore.setState({ user: adminAccount });
    vi.mocked(hasUserReviewed).mockResolvedValue(false);
    vi.mocked(createReview).mockRejectedValue(new Error("There was an error while creating the review."));

    const RouterStub = createRoutesStub([
      {
        path: "/components/:id",
        Component: ComponentDetail,
        loader: () => ({
          component: component1,
          reviews: { items: [], hasNext: false },
          hasReviewed: false,
        }),
      },
    ]);

    render(<RouterStub initialEntries={["/components/1"]} />);

    await screen.findByText("AMD Ryzen 7 7800X3D");
    await user.type(screen.getByLabelText("Título:"), "sample_title");
    await user.type(screen.getByLabelText("Mensaje:"), "sample_message");
    await user.type(screen.getByLabelText("Valoración:"), "5");
    await user.click(screen.getByRole("button", { name: /Guardar reseña/i }));

    await waitFor(() => {
      expect(createReview).toHaveBeenCalledWith(1, "sample_title", "sample_message", 5);
      expect(screen.getByText("There was an error while creating the review.")).toBeInTheDocument();
      expect(screen.getByText("¡Añade una reseña a este producto!")).toBeInTheDocument();
    });

  });

  it("it does not show the review delete button for non-registered users", async () => {

    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component2, reviews: { items: [review1, review2], hasNext: false } }) },
    ]);

    render(<RouterStub initialEntries={["/components/2"]} />);

    await waitFor(() => {
      expect(document.getElementById("delete-review-button-1")).not.toBeInTheDocument();
      expect(document.getElementById("delete-review-button-2")).not.toBeInTheDocument();
    });

  });

  it("shows only the delete review button for logged-in non-admin users for own reviews", async () => {

    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component2, reviews: { items: [review1, review2], hasNext: false } }) },
    ]);

    vi.mocked(reqIsLogged).mockResolvedValue(userAccount);
    useUserStore.setState({ user: userAccount });

    render(<RouterStub initialEntries={["/components/2"]} />);

    await waitFor(() => {
      expect(document.getElementById("delete-review-button-1")).toBeInTheDocument();
      expect(document.getElementById("delete-review-button-2")).not.toBeInTheDocument();
    });

  });

  it("shows the review delete button for admin users for all reviews", async () => {

    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component2, reviews: { items: [review1, review2], hasNext: false } }) },
    ]);

    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);
    useUserStore.setState({ user: adminAccount });

    render(<RouterStub initialEntries={["/components/2"]} />);

    await waitFor(() => {
      expect(document.getElementById("delete-review-button-1")).toBeInTheDocument();
      expect(document.getElementById("delete-review-button-2")).toBeInTheDocument();
    });

  });

  it("deletes review", async () => {
    const user = userEvent.setup();
    let reviews = [review1, review2];

    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component2, reviews: { items: reviews, hasNext: false } }) },
    ]);

    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);
    useUserStore.setState({ user: adminAccount });

    vi.mocked(deleteReview).mockImplementation(async () => {
      reviews = reviews.filter((review) => review.id !== review1.id);
    });

    render(<RouterStub initialEntries={["/components/2"]} />);

    await waitFor(() => {
      expect(document.getElementById("delete-review-button-1")).toBeInTheDocument();
    });
    await user.click(document.getElementById("delete-review-button-1")!);

    expect(await screen.findByText("¿Está seguro de que desea borrar esta reseña?")).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Cancelar/ })).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Eliminar reseña/ })).toBeInTheDocument();

    await user.click(await screen.findByRole("button", { name: /Eliminar reseña/ }));

    await waitFor(() => {
      expect(deleteReview).toHaveBeenCalledWith(component2.id, review1.id);
      expect(screen.queryByText(review1.title)).not.toBeInTheDocument();
    });

  });

  it("cancel review deletion", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component2, reviews: { items: [review1, review2], hasNext: false } }) },
    ]);

    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);
    useUserStore.setState({ user: adminAccount });

    render(<RouterStub initialEntries={["/components/2"]} />);

    await waitFor(() => {
      expect(document.getElementById("delete-review-button-1")).toBeInTheDocument();
    });
    await user.click(document.getElementById("delete-review-button-1")!);

    expect(await screen.findByText("¿Está seguro de que desea borrar esta reseña?")).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Cancelar/ })).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Eliminar reseña/ })).toBeInTheDocument();

    await user.click(await screen.findByRole("button", { name: /Cancelar/ }));

    await waitFor(() => {
      expect(deleteReview).not.toHaveBeenCalled();
      expect(screen.queryByText(review1.title)).toBeInTheDocument();
    });

  });

  it("cancel review deletion with the close button", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/components/:id", Component: ComponentDetail, loader: () => ({ component: component2, reviews: { items: [review1, review2], hasNext: false } }) },
    ]);

    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);
    useUserStore.setState({ user: adminAccount });

    render(<RouterStub initialEntries={["/components/2"]} />);

    await waitFor(() => {
      expect(document.getElementById("delete-review-button-1")).toBeInTheDocument();
    });
    await user.click(document.getElementById("delete-review-button-1")!);

    expect(await screen.findByText("¿Está seguro de que desea borrar esta reseña?")).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Cancelar/ })).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Eliminar reseña/ })).toBeInTheDocument();

    await user.click(await screen.findByRole("button", { name: "Close" }));

    await waitFor(() => {
      expect(deleteReview).not.toHaveBeenCalled();
      expect(screen.queryByText(review1.title)).toBeInTheDocument();
    });
  });

});