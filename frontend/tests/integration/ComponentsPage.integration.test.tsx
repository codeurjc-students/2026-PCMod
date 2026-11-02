import { render, screen, waitFor } from "@testing-library/react";
import fetchCookie from "fetch-cookie";
import { CookieJar } from "tough-cookie";
import { describe, it, expect, vi } from "vitest";
import { afterEach, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import userEvent from "@testing-library/user-event";
import { createRoutesStub, MemoryRouter, Navigate } from "react-router";
import Components, { clientLoader } from "~/routes/components";
import Login from "~/routes/login";
import ComponentsCreate from "~/routes/components-create";

describe("ComponentsPageIntegration", () => {

  beforeEach(() => {
    vi.stubGlobal("fetch", fetchCookie(globalThis.fetch, new CookieJar()));
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("renders initial components and checks load more functionality", async () => {

    const user = userEvent.setup();

    const initialData = await clientLoader({
      request: new Request("https://localhost:443/components?page=0"),
      params: {},
      context: {},
      url: new URL("https://localhost:443/components?page=0"),
      pattern: { path: "/components", caseSensitive: false, end: true },
      serverLoader: async () => undefined,
    } as any);

    render(
      <MemoryRouter>
        <Components
          {...({
            loaderData: initialData,
            params: {},
            matches: [],
          } as any)}
        />
      </MemoryRouter>
    );

    await waitFor(() => {
      const initialComponentsNames = screen.getAllByRole("heading", { level: 3 });
      expect(initialComponentsNames).toHaveLength(10);
      expect(initialComponentsNames[0]).toHaveTextContent("AMD Ryzen 7 7800X3D");
    });

    const buttonHasNextTrue = screen.getByRole("button", { name: /Cargar más componentes/i });
    expect(buttonHasNextTrue).toBeInTheDocument();
    await user.click(buttonHasNextTrue);

    await waitFor(() => {
      const updatedComponentsNames = screen.getAllByRole("heading", { level: 3 });
      expect(updatedComponentsNames.length).toBeGreaterThan(10);
    });

    const buttonHasNextFalse = screen.queryByRole("button", { name: /Cargar más componentes/i });
    expect(buttonHasNextFalse).not.toBeInTheDocument();

  });

  it("renders an empty state when there are no components", async () => {

    render(
      <Components
        {...({
          loaderData: {
            items: [],
            hasNext: false,
          },
          params: {},
          matches: [],
        } as any)}
      />
    );

    await waitFor(() => {
      expect(screen.getByText("No se han encontrado componentes de PC.")).toBeInTheDocument();
    });

    expect(screen.queryAllByRole("heading", { level: 3 })).toHaveLength(0);
    expect(screen.queryByRole("button", { name: /Cargar más componentes/i })).not.toBeInTheDocument();
  });

  it("shows an error when loading more components fails", async () => {
    const user = userEvent.setup();
    const firstComponent = {
      id: 1,
      name: "AMD Ryzen 7 7800X3D",
      description: "Procesador gaming de alto rendimiento.",
      type: "CPU",
      brand: "AMD",
      price: 350.0,
      stock: 5,
    };

    const fetchSpy = vi.spyOn(globalThis, "fetch").mockRejectedValueOnce(new Error("Network error"));

    render(
      <MemoryRouter>
        <Components
          {...({
            loaderData: {
              items: [firstComponent],
              hasNext: true,
            },
            params: {},
            matches: [],
          } as any)}
        />
      </MemoryRouter>
    );

    await user.click(screen.getByRole("button", { name: /Cargar más componentes/i }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent(
        "Hubo un problema al cargar más componentes. Por favor, pruebe de nuevo.",
      );
    });
    expect(screen.getByRole("button", { name: /Cargar más componentes/i })).toBeInTheDocument();
    expect(fetchSpy).toHaveBeenCalledTimes(1);
    fetchSpy.mockRestore();
  });

  it("does not show the component create button for non-registered users", async () => {
    render(
        <MemoryRouter>
          <Components
            {...({
              loaderData: {
                items: [],
                hasNext: false,
              },
              params: {},
              matches: [],
            } as any)}
          />
        </MemoryRouter>
      );
  
      await waitFor(() => {
        expect(document.getElementById("create-component-button")).not.toBeInTheDocument();
      });
    });


it("does not show the component create button for registered users no admin", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components" replace /> },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(document.getElementById("create-component-button")).not.toBeInTheDocument();
    });
  });

  it("shows the component create button for admin users", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components" replace /> },
      { path: "/components", Component: Components, loader: () => ({ items: [], hasNext: false }) },
      { path: "/components/create", Component: ComponentsCreate, loader: () => ({ componentTypes: ["CPU", "GPU", "RAM", "STORAGE", "MOTHERBOARD", "PSU", "CASE"] }) },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "adminpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(screen.getByRole("link", { name: /Crear componente/i })).toBeInTheDocument();
    });

    await user.click(screen.getByRole("link", { name: /Crear componente/i }));

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: /Ingrese los datos del componente/i })).toBeInTheDocument();
    });
  });

});