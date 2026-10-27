import { render, screen, waitFor } from "@testing-library/react";
import fetchCookie from "fetch-cookie";
import userEvent from "@testing-library/user-event";
import { createRoutesStub, MemoryRouter, Navigate } from "react-router";
import { beforeEach, afterEach, describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import { CookieJar } from "tough-cookie";
import ComponentDetail, { clientLoader } from "~/routes/detail-component";
import Login from "~/routes/login";
import Components from "~/routes/components";
import { getComponents } from "~/services/components-service";

describe("ComponentDetailPageIntegration", () => {

  beforeEach(() => {
    vi.stubGlobal("fetch", fetchCookie(globalThis.fetch, new CookieJar()));

  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("renders the component detail page", async () => {
    const componentData = await clientLoader({ params: { id: "1" } } as Parameters<typeof clientLoader>[0]);

    render(
      <MemoryRouter>
        <ComponentDetail
          {...({ loaderData: componentData, params: {}, matches: [] } as any)}
        />
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText(componentData.name)).toBeInTheDocument();
      expect(screen.getByRole("paragraph")).toHaveTextContent("8 núcleos y 16 hilos ideales para gaming");
      expect(screen.getByText(`${componentData.price.toFixed(2)} €`)).toBeInTheDocument();
      expect(screen.getByRole("heading", { name: `${componentData.brand} | ${componentData.type}` })).toBeInTheDocument();
      expect(screen.getByText("Reseñas del producto:")).toBeInTheDocument();
      expect(screen.getByText("No hay reseñas de este producto todavía.")).toBeInTheDocument();
    });
  });

  it("renders the last units warning when stock is low", async () => {
    const componentData = await clientLoader({ params: { id: "1" } } as Parameters<typeof clientLoader>[0]);

    render(
      <MemoryRouter>
        <ComponentDetail
          {...({ loaderData: componentData, params: {}, matches: [] } as any)}
        />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText("¡Últimas unidades!")).toBeInTheDocument();
    });
  });

  it("does not render the last units warning when stock is not low", async () => {
    const componentData = await clientLoader({ params: { id: "3" } } as Parameters<typeof clientLoader>[0]);

    render(
      <MemoryRouter>
        <ComponentDetail
          {...({ loaderData: componentData, params: {}, matches: [] } as any)}
        />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.queryByText("¡Últimas unidades!")).not.toBeInTheDocument();
    });
  });

  it("shows only the component add to cart button for non-registered users", async () => {
    const componentData = await clientLoader({ params: { id: "1" } } as Parameters<typeof clientLoader>[0]);

    render(
      <MemoryRouter>
        <ComponentDetail
          {...({ loaderData: componentData, params: {}, matches: [] } as any)}
        />
      </MemoryRouter>
    );

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
      { path: "/", Component: () => <Navigate to="/components/10" replace /> },
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
      expect(screen.queryByText("Seagate BarraCuda 3.5")).not.toBeInTheDocument();
    });

  });

});