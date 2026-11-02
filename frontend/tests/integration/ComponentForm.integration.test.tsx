import { render, screen, waitFor } from "@testing-library/react";
import fetchCookie from "fetch-cookie";
import userEvent from "@testing-library/user-event";
import { createRoutesStub, Navigate } from "react-router";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import "@testing-library/jest-dom";
import { CookieJar } from "tough-cookie";
import Login from "~/routes/login";
import ComponentsCreate from "~/routes/components-create";
import Components from "~/routes/components";
import ComponentDetail, { clientLoader } from "~/routes/detail-component";

describe("ProfileIntegration", () => {

  beforeEach(() => {
    vi.stubGlobal("fetch", fetchCookie(globalThis.fetch, new CookieJar()));
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("renders the create component form", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/create" replace /> },
      { path: "/components/create", Component: ComponentsCreate },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "adminpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(screen.getByLabelText("Nombre:")).toBeInTheDocument();
      expect(screen.getByLabelText("Descripción:")).toBeInTheDocument();
      expect(screen.getByLabelText("Tipo de componente:")).toBeInTheDocument();
      expect(screen.getByLabelText("Marca:")).toBeInTheDocument();
      expect(screen.getByLabelText("Precio:")).toBeInTheDocument();
      expect(screen.getByLabelText("Stock:")).toBeInTheDocument();
      expect(screen.getByLabelText("Imagen del componente:")).toBeInTheDocument();
    });
  });

  it("creates a new component with valid data", async () => {

    const user = userEvent.setup();

    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/create" replace /> },
      { path: "/components/create", Component: ComponentsCreate },
      { path: "/components/:id", Component: ComponentDetail, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "adminpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    const file = new File(["dummy content"], "user.jpg", { type: "image/jpeg" });

    await waitFor(async () => {
      expect(screen.getByLabelText("Nombre:")).toBeInTheDocument();
    });
    await user.type(screen.getByLabelText("Nombre:"), "testName");
    await user.type(screen.getByLabelText("Descripción:"), "testDescription");
    await user.type(screen.getByLabelText("Marca:"), "testBrand");
    await screen.findByRole("option", { name: "Procesador" });
    await user.selectOptions(screen.getByLabelText("Tipo de componente:"), "CPU");
    await user.type(screen.getByLabelText("Precio:"), "10");
    await user.type(screen.getByLabelText("Stock:"), "5");
    await user.upload(screen.getByLabelText("Imagen del componente:"), file);

    const createButton = await screen.findByRole("button", { name: /Guardar componente/i });
    await user.click(createButton);

    await waitFor(() => {
      expect(screen.getByText("testName")).toBeInTheDocument();
      expect(screen.getByText("testDescription")).toBeInTheDocument();
      expect(screen.getByText(/testBrand/i)).toBeInTheDocument();
      expect(screen.getByText(/CPU/i)).toBeInTheDocument();
      expect(screen.getByText(/testBrand/i)).toBeInTheDocument();
      expect(screen.getByText(/10\.00/i)).toBeInTheDocument();
    });

  });

  it("shows validation errors for void data", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/create" replace /> },
      { path: "/components/create", Component: ComponentsCreate },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "adminpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(screen.getByLabelText("Nombre:")).toBeInTheDocument();
    });
    await user.clear(screen.getByLabelText("Nombre:"));
    await user.clear(screen.getByLabelText("Descripción:"));
    await user.clear(screen.getByLabelText("Marca:"));
    await user.clear(screen.getByLabelText("Precio:"));
    await user.clear(screen.getByLabelText("Stock:"));

    const createButton = await screen.findByRole("button", { name: /Guardar componente/i });
    await user.click(createButton);

    await waitFor(() => {
      expect(screen.getByText("Por favor, ingrese un nombre válido.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese una descripción válida.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, seleccione un tipo válido.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese una marca válida.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese un precio válido.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese un stock válido.")).toBeInTheDocument();
    });

  });

  it("shows validation errors for existing data", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/create" replace /> },
      { path: "/components/create", Component: ComponentsCreate },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "adminpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(async () => {
      expect(screen.getByLabelText("Nombre:")).toBeInTheDocument();
    });
    await user.type(screen.getByLabelText("Nombre:"), "Kingston NV3");
    await user.type(screen.getByLabelText("Descripción:"), "testDescription");
    await user.type(screen.getByLabelText("Marca:"), "testBrand");
    await screen.findByRole("option", { name: "Almacenamiento" });
    await user.selectOptions(screen.getByLabelText("Tipo de componente:"), "STORAGE");
    await user.type(screen.getByLabelText("Precio:"), "10");
    await user.type(screen.getByLabelText("Stock:"), "5");

    const createButton = await screen.findByRole("button", { name: /Guardar componente/i });
    await user.click(createButton);

    await waitFor(() => {
      expect(screen.getByText(/Ya existe un componente con ese nombre\./i)).toBeInTheDocument();
    });

  });

  it("shows validation errors for invalid format data", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/create" replace /> },
      { path: "/components/create", Component: ComponentsCreate },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "adminpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(screen.getByLabelText("Nombre:")).toBeInTheDocument();
    });
    await user.type(screen.getByLabelText("Nombre:"), "testComponent");
    await user.type(screen.getByLabelText("Descripción:"), "testDescription");
    await screen.findByRole("option", { name: "Procesador" });
    await user.selectOptions(screen.getByLabelText("Tipo de componente:"), "CPU");
    await user.type(screen.getByLabelText("Marca:"), "testBrand");
    await user.type(screen.getByLabelText("Precio:"), "-10");
    await user.type(screen.getByLabelText("Stock:"), "-5");

    const createButton = await screen.findByRole("button", { name: /Guardar componente/i });
    await user.click(createButton);
    await waitFor(() => {
      expect(screen.getByText("Por favor, ingrese un precio válido.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese un stock válido.")).toBeInTheDocument();
    });

  });

  it("show error when creating a component with invalid image format", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/create" replace /> },
      { path: "/components/create", Component: ComponentsCreate },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    const file = new File(["dummy image"], "user.txt", { type: "text/plain" });

    await waitFor(async () => {
      expect(screen.getByLabelText("Nombre:")).toBeInTheDocument();
    });
    await user.type(screen.getByLabelText("Nombre:"), "testName");
    await user.type(screen.getByLabelText("Descripción:"), "testDescription");
    await user.type(screen.getByLabelText("Marca:"), "testBrand");
    await screen.findByRole("option", { name: "Procesador" });
    await user.selectOptions(screen.getByLabelText("Tipo de componente:"), "CPU");
    await user.type(screen.getByLabelText("Precio:"), "10");
    await user.type(screen.getByLabelText("Stock:"), "5");
    await user.upload(screen.getByLabelText("Imagen del componente:"), file);

    const createButton = await screen.findByRole("button", { name: /Guardar componente/i });
    await user.click(createButton);

    await waitFor(() => {
      expect(screen.getByText("Por favor, seleccione un archivo de imagen válido.")).toBeInTheDocument();
    });

  });

    it("cancels component creation", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/components/create" replace /> },
      { path: "/components/create", Component: ComponentsCreate },
      { path: "/components", Component: Components, loader: () => ({ items: [], hasNext: false })},
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "admin@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "adminpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    const cancelButton = await screen.findByRole("link", { name: /Cancelar/i });
    await user.click(cancelButton);

    await waitFor(() => {
      expect(screen.getByText(/Componentes:/i)).toBeInTheDocument();
    });

  });

});