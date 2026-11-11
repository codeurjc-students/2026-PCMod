import { render, screen, waitFor } from "@testing-library/react";
import fetchCookie from "fetch-cookie";
import userEvent from "@testing-library/user-event";
import { createMemoryRouter, createRoutesStub, Navigate, RouterProvider } from "react-router";
import { beforeEach, afterEach, describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import { CookieJar } from "tough-cookie";
import ComponentDetail, { clientLoader } from "~/routes/detail-component";
import Login from "~/routes/login";
import Components from "~/routes/components";
import { getComponents } from "~/services/components-service";
import { getPublicUser } from "~/services/users-service";
import { useUserStore } from "~/stores/user-store";

describe("ComponentDetailPageIntegration", () => {

  beforeEach(() => {
    vi.stubGlobal("fetch", fetchCookie(globalThis.fetch, new CookieJar()));
    useUserStore.setState({ user: null, loginError: null });
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("renders the component detail page without reviews", async () => {
    const loadedData = await clientLoader({ params: { id: "1" } } as Parameters<typeof clientLoader>[0]);

    const router = createMemoryRouter(
      [{
          path: "/components/:id",
          element: <ComponentDetail loaderData={loadedData} />,
      }],
      {
        initialEntries: ["/components/1"],
      }
    );

    render(<RouterProvider router={router} />);

    await waitFor(() => {
      expect(screen.getByText(loadedData.component.name)).toBeInTheDocument();
      expect(screen.getByRole("paragraph")).toHaveTextContent("8 núcleos y 16 hilos ideales para gaming");
      expect(screen.getByText(`${loadedData.component.price.toFixed(2)} €`)).toBeInTheDocument();
      expect(screen.getByRole("heading", { name: `${loadedData.component.brand} | ${loadedData.component.type}` })).toBeInTheDocument();
      expect(screen.getByText("Reseñas del producto:")).toBeInTheDocument();
      expect(screen.getByText("No hay reseñas de este producto todavía.")).toBeInTheDocument();
      expect(document.getElementById("loadMore")).not.toBeInTheDocument();
    });
  });

  it("renders the component detail page with reviews", async () => {
    const loadedData = await clientLoader({ params: { id: "2" } } as Parameters<typeof clientLoader>[0]);

    const router = createMemoryRouter(
      [{
          path: "/components/:id",
          element: <ComponentDetail loaderData={loadedData} />,
      }],
      {
        initialEntries: ["/components/2"],
      }
    );

    render(<RouterProvider router={router} />);

    await waitFor(() => {
      expect(screen.getByText(loadedData.component.name)).toBeInTheDocument();
      expect(screen.getByRole("paragraph")).toHaveTextContent("Ray Tracing y DLSS 4 para gaming fluido");
      expect(screen.getByText(`${loadedData.component.price.toFixed(2)} €`)).toBeInTheDocument();
      expect(screen.getByRole("heading", { name: `${loadedData.component.brand} | ${loadedData.component.type}` })).toBeInTheDocument();
      expect(screen.getByText("Reseñas del producto:")).toBeInTheDocument();
      expect(document.getElementById("component-average-rating")).toHaveTextContent("Puntuación media: ★★★★☆ 3.5/5 (2 reseña(s))");
      expect(screen.getByText("user_example")).toBeInTheDocument();
      expect(screen.getByText("Buena gráfica, aunque con cosas a mejorar.")).toBeInTheDocument();
      expect(screen.getByText("Valoración del producto: ★★★★☆ 4/5")).toBeInTheDocument();
      expect(screen.getByText("admin_example")).toBeInTheDocument();
      expect(screen.getByText("Tarjeta gráfica bastante mejorable.")).toBeInTheDocument();
      expect(screen.getByText("Valoración del producto: ★★★☆☆ 3/5")).toBeInTheDocument();
      expect(document.getElementById("loadMore")).not.toBeInTheDocument();
    });
  });

  it("gets public user info", async () => {

    const publicUser = await getPublicUser(1);

    expect(publicUser.id).toBe(1);
    expect(publicUser.username).toBe("user_example");

  });

  it("renders the last units warning when stock is low", async () => {
    const componentData = await clientLoader({ params: { id: "1" } } as Parameters<typeof clientLoader>[0]);

    const router = createMemoryRouter(
      [{
          path: "/components/:id",
          element: <ComponentDetail loaderData={componentData} />,
      }],
      {
        initialEntries: ["/components/1"],
      }
    );

    render(<RouterProvider router={router} />);

    await waitFor(() => {
      expect(screen.getByText("¡Últimas unidades!")).toBeInTheDocument();
    });
  });

  it("does not render the last units warning when stock is not low", async () => {
    const componentData = await clientLoader({ params: { id: "3" } } as Parameters<typeof clientLoader>[0]);

    const router = createMemoryRouter(
      [{
          path: "/components/:id",
          element: <ComponentDetail loaderData={componentData} />,
      }],
      {
        initialEntries: ["/components/1"],
      }
    );

    render(<RouterProvider router={router} />);

    await waitFor(() => {
      expect(screen.queryByText("¡Últimas unidades!")).not.toBeInTheDocument();
    });
  });

  it("renders no stock warning when stock is 0", async () => {
    const componentData = await clientLoader({ params: { id: "4" } } as Parameters<typeof clientLoader>[0]);

    const router = createMemoryRouter(
      [{
          path: "/components/:id",
          element: <ComponentDetail loaderData={componentData} />,
      }],
      {
        initialEntries: ["/components/1"],
      }
    );

    render(<RouterProvider router={router} />);
  
    await waitFor(() => {
      expect(screen.getByText("¡No hay stock!")).toBeInTheDocument();
      expect(document.getElementById("add-to-cart-button")).toBeDisabled();
    });

  });

  it("shows only the component add to cart button for non-registered users", async () => {
    const componentData = await clientLoader({ params: { id: "1" } } as Parameters<typeof clientLoader>[0]);

    const router = createMemoryRouter(
      [{
          path: "/components/:id",
          element: <ComponentDetail loaderData={componentData} />,
      }],
      {
        initialEntries: ["/components/1"],
      }
    );

    render(<RouterProvider router={router} />);

    await waitFor(() => {
      expect(document.getElementById("add-to-cart-button")).toBeInTheDocument();
      expect(document.getElementById("edit-component-button")).not.toBeInTheDocument();
      expect(document.getElementById("delete-component-button")).not.toBeInTheDocument();
    });
  });

  it("shows only the component add to cart button for logged-in non-admin users", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/1" replace /> },
      { path: "/components/:id", Component: ComponentDetail, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(document.getElementById("add-to-cart-button")).toBeInTheDocument();
      expect(document.getElementById("edit-component-button")).not.toBeInTheDocument();
      expect(document.getElementById("delete-component-button")).not.toBeInTheDocument();
    });
  });

  it("shows the component edit and delete buttons for admin users", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/1" replace /> },
      { path: "/components/:id", Component: ComponentDetail, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "adminpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(document.getElementById("add-to-cart-button")).toBeInTheDocument();
      expect(document.getElementById("edit-component-button")).toBeInTheDocument();
      expect(document.getElementById("delete-component-button")).toBeInTheDocument();
    });
  });

  it("cancel component deletion", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/1" replace /> },
      { path: "/components/:id", Component: ComponentDetail, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "adminpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(document.getElementById("delete-component-button")).toBeInTheDocument();
    });
    await user.click(document.getElementById("delete-component-button")!);
  
    expect(await screen.findByText("¿Está seguro de que desea borrar este componente?")).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Cancelar/ })).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Eliminar componente/ })).toBeInTheDocument();
  
    await user.click(await screen.findByRole("button", { name: /Cancelar/ }));
  
    await waitFor(() => {
      expect(screen.queryByText("AMD Ryzen 7 7800X3D")).toBeInTheDocument();
    });

  });

  it("cancel component deletion with the close button", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/1" replace /> },
      { path: "/components/:id", Component: ComponentDetail, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "adminpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(document.getElementById("delete-component-button")).toBeInTheDocument();
    });
    await user.click(document.getElementById("delete-component-button")!);
  
    expect(await screen.findByText("¿Está seguro de que desea borrar este componente?")).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Cancelar/ })).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Eliminar componente/ })).toBeInTheDocument();
  
    await user.click(await screen.findByRole("button", { name: "Close" }));
  
    await waitFor(() => {
      expect(screen.queryByText("¿Está seguro de que desea borrar este componente?")).not.toBeInTheDocument();
      expect(screen.queryByText("AMD Ryzen 7 7800X3D")).toBeInTheDocument();
    });

  });

  it("deletes component", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/8" replace /> },
      { path: "/components/:id", Component: ComponentDetail, loader: clientLoader },
      { path: "/components", Component: Components,
        loader: async ({ request }) => {
          const url = new URL(request.url);
          const page = Number(url.searchParams.get("page") ?? 0);
          const result = await getComponents(page);
          return { items: result.items, hasNext: result.hasNext };
        }
      }
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "adminpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(document.getElementById("delete-component-button")).toBeInTheDocument();
    });
    await user.click(document.getElementById("delete-component-button")!);
  
    expect(await screen.findByText("¿Está seguro de que desea borrar este componente?")).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Cancelar/ })).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Eliminar componente/ })).toBeInTheDocument();
  
    await user.click(await screen.findByRole("button", { name: /Eliminar componente/ }));
  
    await waitFor(() => {
      expect(screen.queryByText("Intel Core i5-12400F")).not.toBeInTheDocument();
    });

  });

  it("it does not show the review delete button for non-registered users", async () => {

    const componentData = await clientLoader({ params: { id: "2" } } as Parameters<typeof clientLoader>[0]);

    const router = createMemoryRouter(
      [{
          path: "/components/:id",
          element: <ComponentDetail loaderData={componentData} />,
      }],
      {
        initialEntries: ["/components/2"],
      }
    );

    render(<RouterProvider router={router} />);

    await waitFor(() => {
      expect(document.getElementById("delete-review-button-1")).not.toBeInTheDocument();
      expect(document.getElementById("delete-review-button-2")).not.toBeInTheDocument();
    });

  });

  it("shows only the delete review button for logged-in non-admin users for own reviews", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/2" replace /> },
      { path: "/components/:id", Component: ComponentDetail, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(document.getElementById("delete-review-button-1")).toBeInTheDocument();
      expect(document.getElementById("delete-review-button-2")).not.toBeInTheDocument();
    });

  });

  it("shows the review delete button for admin users for all reviews", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/2" replace /> },
      { path: "/components/:id", Component: ComponentDetail, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "adminpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(document.getElementById("delete-review-button-1")).toBeInTheDocument();
      expect(document.getElementById("delete-review-button-2")).toBeInTheDocument();
    });

  });

  it("does not render the review form when user is not logged in", async () => {

    const RouterStub = createRoutesStub([,
      { path: "/components/:id", Component: ComponentDetail, loader: clientLoader }
    ]);

    render(<RouterStub initialEntries={["/components/1"]} />);

    await waitFor(() => {
      expect(screen.queryByText("¡Añade una reseña a este producto!")).not.toBeInTheDocument();
    });

  });

  it("does not render the review form when user has already reviewed the component", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/2" replace /> },
      { path: "/components/:id", Component: ComponentDetail, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "adminpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(screen.queryByText("¡Añade una reseña a este producto!")).not.toBeInTheDocument();
    });

  });

  it("renders the review form when user has not reviewed the component", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/1" replace /> },
      { path: "/components/:id", Component: ComponentDetail, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "adminpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(screen.queryByText("¡Añade una reseña a este producto!")).toBeInTheDocument();
      expect(screen.getByLabelText("Título:")).toBeInTheDocument();
      expect(screen.getByLabelText("Mensaje:")).toBeInTheDocument();
      expect(screen.getByLabelText("Valoración:")).toBeInTheDocument();
    });

  });

  it("creates a new review with valid data", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/5" replace /> },
      { path: "/components/:id", Component: ComponentDetail, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "adminpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await screen.findByText("MAG A650BN");
    await user.type(screen.getByLabelText("Título:"), "sample_title");
    await user.type(screen.getByLabelText("Mensaje:"), "sample_message");
    await user.type(screen.getByLabelText("Valoración:"), "5");

    const createButton = await screen.findByRole("button", { name: /Guardar reseña/i });
    await user.click(createButton);

    await waitFor(() => {
      expect(screen.getByText(/5\/5 \(1 reseña\(s\)\)/i)).toBeInTheDocument();
      expect(screen.getByText("sample_title")).toBeInTheDocument();
      expect(screen.getByText("sample_message")).toBeInTheDocument();
      expect(screen.getByText("Valoración del producto: ★★★★★ 5/5")).toBeInTheDocument();
    });

  });

  it("show error when creating a new review with void data", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/5" replace /> },
      { path: "/components/:id", Component: ComponentDetail, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await screen.findByText("MAG A650BN");

    const createButton = await screen.findByRole("button", { name: /Guardar reseña/i });
    await user.click(createButton);

    await waitFor(() => {
      expect(screen.getByText("Por favor, ingrese un título válido.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese un mensaje válido.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese una valoración válida.")).toBeInTheDocument();
    });

  });

  it("show error when creating a new review with invalid data", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/5" replace /> },
      { path: "/components/:id", Component: ComponentDetail, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await screen.findByText("MAG A650BN");
    await user.type(screen.getByLabelText("Título:"), "sample_title");
    await user.type(screen.getByLabelText("Mensaje:"), "sample_message");
    await user.type(screen.getByLabelText("Valoración:"), "-5");

    const createButton = await screen.findByRole("button", { name: /Guardar reseña/i });
    await user.click(createButton);

    await waitFor(() => {
      expect(screen.getByText("Por favor, ingrese una valoración válida.")).toBeInTheDocument();
    });

  });

  it("cancel review deletion", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/2" replace /> },
      { path: "/components/:id", Component: ComponentDetail, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "adminpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(document.getElementById("delete-review-button-1")).toBeInTheDocument();
    });
    await user.click(document.getElementById("delete-review-button-1")!);

    expect(await screen.findByText("¿Está seguro de que desea borrar esta reseña?")).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Cancelar/ })).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Eliminar reseña/ })).toBeInTheDocument();

    await user.click(await screen.findByRole("button", { name: /Cancelar/ }));

    await waitFor(() => {
      expect(screen.queryByText("Buena gráfica, aunque con cosas a mejorar.")).toBeInTheDocument();
    });

  });

  it("cancel review deletion with the close button", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/2" replace /> },
      { path: "/components/:id", Component: ComponentDetail, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "adminpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(document.getElementById("delete-review-button-1")).toBeInTheDocument();
    });
    await user.click(document.getElementById("delete-review-button-1")!);

    expect(await screen.findByText("¿Está seguro de que desea borrar esta reseña?")).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Cancelar/ })).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Eliminar reseña/ })).toBeInTheDocument();

    await user.click(await screen.findByRole("button", { name: "Close" }));

    await waitFor(() => {
      expect(screen.queryByText("Buena gráfica, aunque con cosas a mejorar.")).toBeInTheDocument();
    });
  });

  it("deletes review", async () => {
    
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/2" replace /> },
      { path: "/components/:id", Component: ComponentDetail, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "adminpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(document.getElementById("delete-review-button-1")).toBeInTheDocument();
    });
    await user.click(document.getElementById("delete-review-button-1")!);

    expect(await screen.findByText("¿Está seguro de que desea borrar esta reseña?")).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Cancelar/ })).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Eliminar reseña/ })).toBeInTheDocument();

    await user.click(await screen.findByRole("button", { name: /Eliminar reseña/ }));

    await waitFor(() => {
      expect(screen.queryByText("Buena gráfica, aunque con cosas a mejorar.")).not.toBeInTheDocument();
    });

  });

});