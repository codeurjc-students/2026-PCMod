import { render, waitFor, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import userEvent from "@testing-library/user-event";
import { createRoutesStub } from "react-router";
import { beforeEach, describe, expect, it, vi } from "vitest";
import ComponentsCreate, { clientLoader } from "~/routes/components-create";
import ComponentDetail from "~/routes/detail-component";
import { addImage, createComponent, getComponent, getComponentTypes, updateComponent } from "~/services/components-service";
import { reqIsLogged } from "~/services/users-service";
import { useUserStore } from "~/stores/user-store";
import { requireAdminUser } from "~/services/route-guard";
import ComponentsUpdate, { clientLoader as updateClientLoader } from "~/routes/components-update";

vi.mock("~/services/users-service", () => ({
  reqIsLogged: vi.fn(),
}));

vi.mock("~/services/components-service", () => ({
  createComponent: vi.fn(),
  addImage: vi.fn(),
  getComponentTypes: vi.fn(),
  getComponent: vi.fn(),
  updateComponent: vi.fn(),
}));

vi.mock("~/services/route-guard", () => ({
  requireAdminUser: vi.fn(),
}));

const adminAccount = {
  id: 2,
  name: "Admin",
  surname: "Example",
  username: "admin@example.com",
  address: "Admin address",
  email: "admin@example.com",
  roles: ["REGISTERED_USER", "ADMIN"],
};

const component = {
  id: 5,
  name: "testComponent",
  description: "testDescription",
  type: "CPU",
  brand: "testBrand",
  price: 10,
  stock: 5,
};

describe("ComponentForm", () => {

  beforeEach(() => {
    vi.clearAllMocks();
    useUserStore.setState({ user: null });
    vi.mocked(reqIsLogged).mockResolvedValue(null as never);
    vi.mocked(getComponentTypes).mockResolvedValue(["CPU", "GPU", "RAM", "MOTHERBOARD", "STORAGE", "PSU", "Case"]);
  });

  it("renders the component form", async () => {
    
    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);

    const RouterStub = createRoutesStub([
      { path: "/components/create", Component: ComponentsCreate },
    ]);

    render(<RouterStub initialEntries={["/components/create"]} />);

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

  it("clientLoader returns the admin user", async () => {
    vi.mocked(requireAdminUser).mockResolvedValue(adminAccount);

    const result = await clientLoader();

    expect(requireAdminUser).toHaveBeenCalledTimes(1);
    expect(result).toEqual(adminAccount);
  });
  
  it("creates a new component with valid credentials", async () => {

    const user = userEvent.setup();
    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);
    
    vi.mocked(createComponent).mockResolvedValue({
      id: 1,
      name: "testComponent",
      description: "testDescription",
      type: "CPU",
      brand: "testBrand",
      price: 10,
      stock: 5,
    });
    vi.mocked(addImage).mockResolvedValue({} as Awaited<ReturnType<typeof addImage>>);

    const RouterStub = createRoutesStub([
      { path: "/components/create", Component: ComponentsCreate },
      { path: "/components/:id", 
        Component: ComponentDetail, 
        loader: async ({ params }) => {
          return {
            id: Number(params.id),
            name: "testComponent",
            description: "testDescription",
            type: "CPU",
            brand: "testBrand",
            price: 10,
            stock: 5,
          };
        }
      }
    ]);

    render(<RouterStub initialEntries={["/components/create"]} />);

    const file = new File(["dummy content"], "component.jpg", { type: "image/jpeg" });

    await user.type(screen.getByLabelText("Nombre:"), "testComponent");
    await user.type(screen.getByLabelText("Descripción:"), "testDescription");
    await screen.findByRole("option", { name: "Procesador" });
    await user.selectOptions(screen.getByLabelText("Tipo de componente:"), "CPU");
    await user.type(screen.getByLabelText("Marca:"), "testBrand");
    await user.type(screen.getByLabelText("Precio:"), "10");
    await user.type(screen.getByLabelText("Stock:"), "5");
    await user.upload(screen.getByLabelText("Imagen del componente:"), file);

    const createButton = await screen.findByRole("button", { name: /Guardar componente/i });
    await user.click(createButton);

    await waitFor(() => {
      expect(createComponent).toHaveBeenCalledTimes(1);
      expect(screen.getByText("testComponent")).toBeInTheDocument();
      expect(screen.getByText("testDescription")).toBeInTheDocument();
      expect(screen.getByText(/testBrand/i)).toBeInTheDocument();
      expect(screen.getByText(/CPU/i)).toBeInTheDocument();
      expect(screen.getByText(/10\.00/i)).toBeInTheDocument();
    });

  });

  it("show error when creating a new component with void data", async () => {

    const user = userEvent.setup();
    
    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);

    const RouterStub = createRoutesStub([
      { path: "/components/create", Component: ComponentsCreate },
    ]);

    render(<RouterStub initialEntries={["/components/create"]} />);

    await user.clear(screen.getByLabelText("Nombre:"));
    await user.clear(screen.getByLabelText("Descripción:"));
    await user.clear(screen.getByLabelText("Marca:"));
    await user.clear(screen.getByLabelText("Precio:"));
    await user.clear(screen.getByLabelText("Stock:"));

    const createButton = await screen.findByRole("button", { name: /Guardar componente/i });
    await user.click(createButton);

    await waitFor(() => {
      expect(createComponent).not.toHaveBeenCalled();
      expect(screen.getByText("Por favor, ingrese un nombre válido.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese una descripción válida.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, seleccione un tipo válido.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese una marca válida.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese un precio válido.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese un stock válido.")).toBeInTheDocument();
    });

  });

  it("show error when creating a new component with existing data", async () => {

    const user = userEvent.setup();
    
    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);

    vi.mocked(createComponent).mockRejectedValue(new Error(
      "Validación fallida:\n• Ya existe un componente con ese nombre."
    ));

    const RouterStub = createRoutesStub([
      { path: "/components/create", Component: ComponentsCreate },
    ]);

    render(<RouterStub initialEntries={["/components/create"]} />);

    await user.type(screen.getByLabelText("Nombre:"), "testComponent");
    await user.type(screen.getByLabelText("Descripción:"), "testDescription");
    await screen.findByRole("option", { name: "Procesador" });
    await user.selectOptions(screen.getByLabelText("Tipo de componente:"), "CPU");
    await user.type(screen.getByLabelText("Marca:"), "testBrand");
    await user.type(screen.getByLabelText("Precio:"), "10");
    await user.type(screen.getByLabelText("Stock:"), "5");

    const createButton = await screen.findByRole("button", { name: /Guardar componente/i });
    await user.click(createButton);

    await waitFor(() => {
      expect(createComponent).toHaveBeenCalledTimes(1);
      expect(screen.getByText(/Ya existe un componente con ese nombre\./i)).toBeInTheDocument();
    });

  });

  it("show error when creating a new component with invalid format", async () => {

    const user = userEvent.setup();

    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);

    const RouterStub = createRoutesStub([
      { path: "/components/create", Component: ComponentsCreate },
    ]);

    render(<RouterStub initialEntries={["/components/create"]} />);

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
      expect(createComponent).not.toHaveBeenCalled();
      expect(screen.getByText("Por favor, ingrese un precio válido.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese un stock válido.")).toBeInTheDocument();
    });

  });

  it("shows error when uploading an image with invalid format", async () => {

    const user = userEvent.setup();

    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);

    const file = new File(["dummy image"], "user.txt", { type: "text/plain" });

    const RouterStub = createRoutesStub([
      { path: "/components/create", Component: ComponentsCreate },
    ]);

    render(<RouterStub initialEntries={["/components/create"]} />);

    await user.type(screen.getByLabelText("Nombre:"), "testComponent");
    await user.type(screen.getByLabelText("Descripción:"), "testDescription");
    await screen.findByRole("option", { name: "Procesador" });
    await user.selectOptions(screen.getByLabelText("Tipo de componente:"), "CPU");
    await user.type(screen.getByLabelText("Marca:"), "testBrand");
    await user.type(screen.getByLabelText("Precio:"), "10");
    await user.type(screen.getByLabelText("Stock:"), "5");
    await user.upload(screen.getByLabelText("Imagen del componente:"), file);

    const createButton = await screen.findByRole("button", { name: /Guardar componente/i });
    await user.click(createButton);

    await waitFor(() => {
      expect(addImage).not.toHaveBeenCalled();
      expect(screen.getByText("Por favor, seleccione un archivo de imagen válido.")).toBeInTheDocument();
    });

  });

  it("cancel component creation", async () => {

    const user = userEvent.setup();

    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);

    const RouterStub = createRoutesStub([
      { path: "/components/create", Component: ComponentsCreate }
    ]);

    render(<RouterStub initialEntries={["/components/create"]} />);

    const cancelButton = await screen.findByRole("link", { name: /Cancelar/ });
    await user.click(cancelButton);

    await waitFor(() => {
      expect(createComponent).not.toHaveBeenCalled();
    });

  });

  it("renders the component form with data when updating a component", async () => {
    
    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);
    vi.mocked(getComponent).mockResolvedValue(component);

    const RouterStub = createRoutesStub([
      { path: "/components/5/update", Component: ComponentsUpdate, loader: updateClientLoader },
    ]);

    render(<RouterStub initialEntries={["/components/5/update"]} />);

    await waitFor(() => {
      expect(screen.getByLabelText("Nombre:")).toBeInTheDocument();
      expect(screen.getByLabelText("Nombre:")).toHaveValue("testComponent");
      expect(screen.getByLabelText("Descripción:")).toBeInTheDocument();
      expect(screen.getByLabelText("Descripción:")).toHaveValue("testDescription");
      expect(screen.getByLabelText("Tipo de componente:")).toBeInTheDocument();
      expect(screen.getByLabelText("Tipo de componente:")).toHaveValue("CPU");
      expect(screen.getByLabelText("Marca:")).toBeInTheDocument();
      expect(screen.getByLabelText("Marca:")).toHaveValue("testBrand");
      expect(screen.getByLabelText("Precio:")).toBeInTheDocument();
      expect(screen.getByLabelText("Precio:")).toHaveValue(10);
      expect(screen.getByLabelText("Stock:")).toBeInTheDocument();
      expect(screen.getByLabelText("Stock:")).toHaveValue(5);
    });

  });

  it("updateClientLoader returns the component to update", async () => {
    vi.mocked(requireAdminUser).mockResolvedValue(adminAccount);

    vi.mocked(getComponent).mockResolvedValue(component);

    const result = await updateClientLoader({ params: { id: "5" } } as never);

    expect(getComponent).toHaveBeenCalledTimes(1);
    expect(requireAdminUser).toHaveBeenCalledTimes(1);
    expect(result).toEqual({ component });
  });

  it("updated a component with valid data", async () => {

    const user = userEvent.setup();
    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);

    vi.mocked(getComponent).mockResolvedValue({
      id: 5,
      name: "testOriginalComponent",
      description: "testOriginalDescription",
      type: "GPU",
      brand: "testOriginalBrand",
      price: 15,
      stock: 10,
    });
    
    vi.mocked(updateComponent).mockResolvedValue(component);
    vi.mocked(addImage).mockResolvedValue({} as Awaited<ReturnType<typeof addImage>>);

    const RouterStub = createRoutesStub([
      { path: "/components/5/update", Component: ComponentsUpdate, loader: updateClientLoader },
      { path: "/components/:id", 
        Component: ComponentDetail, 
        loader: async ({ params }) => {
          return {
            id: Number(params.id),
            name: "testComponent",
            description: "testDescription",
            type: "CPU",
            brand: "testBrand",
            price: 10,
            stock: 5,
          };
        }
      }
    ]);

    render(<RouterStub initialEntries={["/components/5/update"]} />);

    const file = new File(["dummy content"], "component.jpg", { type: "image/jpeg" });

    await screen.findByLabelText("Nombre:");
    await user.clear(screen.getByLabelText("Nombre:"));
    await user.type(screen.getByLabelText("Nombre:"), "testComponent");
    await user.clear(screen.getByLabelText("Descripción:"));
    await user.type(screen.getByLabelText("Descripción:"), "testDescription");
    await screen.findByRole("option", { name: "Procesador" });
    await user.selectOptions(screen.getByLabelText("Tipo de componente:"), "CPU");
    await user.type(screen.getByLabelText("Marca:"), "testBrand");
    await user.type(screen.getByLabelText("Precio:"), "10");
    await user.type(screen.getByLabelText("Stock:"), "5");
    await user.upload(screen.getByLabelText("Imagen del componente:"), file);

    const updateButton = await screen.findByRole("button", { name: /Actualizar componente/i });
    await user.click(updateButton);

    await waitFor(() => {
      expect(updateComponent).toHaveBeenCalledTimes(1);
      expect(screen.getByText("testComponent")).toBeInTheDocument();
      expect(screen.getByText("testDescription")).toBeInTheDocument();
      expect(screen.getByText(/testBrand/i)).toBeInTheDocument();
      expect(screen.getByText(/CPU/i)).toBeInTheDocument();
      expect(screen.getByText(/10\.00/i)).toBeInTheDocument();
    });

  });

  it("show error when updating a component with void data", async () => {

    const user = userEvent.setup();
    
    vi.mocked(getComponent).mockResolvedValue(component);
    
    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);

    const RouterStub = createRoutesStub([
      { path: "/components/5/update", Component: ComponentsUpdate, loader: updateClientLoader },
    ]);

    render(<RouterStub initialEntries={["/components/5/update"]} />);

    await screen.findByLabelText("Nombre:");
    await user.clear(screen.getByLabelText("Nombre:"));
    await user.clear(screen.getByLabelText("Descripción:"));
    await user.selectOptions(screen.getByLabelText("Tipo de componente:"), "");
    await user.clear(screen.getByLabelText("Marca:"));
    await user.clear(screen.getByLabelText("Precio:"));
    await user.clear(screen.getByLabelText("Stock:"));

    const updateButton = await screen.findByRole("button", { name: /Actualizar componente/i });
    await user.click(updateButton);

    await waitFor(() => {
      expect(updateComponent).not.toHaveBeenCalled();
      expect(screen.getByText("Por favor, ingrese un nombre válido.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese una descripción válida.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, seleccione un tipo válido.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese una marca válida.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese un precio válido.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese un stock válido.")).toBeInTheDocument();
    });

  });

  it("show error when updating a component with existing data", async () => {

    const user = userEvent.setup();

    vi.mocked(getComponent).mockResolvedValue(component);
    
    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);

    vi.mocked(updateComponent).mockRejectedValue(new Error(
      "Validación fallida:\n• Ya existe un componente con ese nombre."
    ));

    const RouterStub = createRoutesStub([
      { path: "/components/5/update", Component: ComponentsUpdate, loader: updateClientLoader },
    ]);

    render(<RouterStub initialEntries={["/components/5/update"]} />);

    await screen.findByLabelText("Nombre:");
    await user.clear(screen.getByLabelText("Nombre:"));
    await user.type(screen.getByLabelText("Nombre:"), "testUpdatedComponent");

    const updateButton = await screen.findByRole("button", { name: /Actualizar componente/i });
    await user.click(updateButton);

    await waitFor(() => {
      expect(updateComponent).toHaveBeenCalledTimes(1);
      expect(screen.getByText(/Ya existe un componente con ese nombre\./i)).toBeInTheDocument();
    });

  });

  it("show error when updating a component with invalid format", async () => {

    const user = userEvent.setup();

    vi.mocked(getComponent).mockResolvedValue(component);

    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);

    const RouterStub = createRoutesStub([
      { path: "/components/5/update", Component: ComponentsUpdate, loader: updateClientLoader },
    ]);

    render(<RouterStub initialEntries={["/components/5/update"]} />);

    await screen.findByLabelText("Nombre:");
    await user.clear(screen.getByLabelText("Precio:"));
    await user.type(screen.getByLabelText("Precio:"), "-10");
    await user.clear(screen.getByLabelText("Stock:"));
    await user.type(screen.getByLabelText("Stock:"), "-5");

    const updateButton = await screen.findByRole("button", { name: /Actualizar componente/i });
    await user.click(updateButton);

    await waitFor(() => {
      expect(updateComponent).not.toHaveBeenCalled();
      expect(screen.getByText("Por favor, ingrese un precio válido.")).toBeInTheDocument();
      expect(screen.getByText("Por favor, ingrese un stock válido.")).toBeInTheDocument();
    });

  });

  it("shows error when updating with an image with invalid format", async () => {

    const user = userEvent.setup();

    vi.mocked(getComponent).mockResolvedValue(component);

    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);

    const file = new File(["dummy image"], "user.txt", { type: "text/plain" });

    const RouterStub = createRoutesStub([
      { path: "/components/5/update", Component: ComponentsUpdate, loader: updateClientLoader },
    ]);

    render(<RouterStub initialEntries={["/components/5/update"]} />);

    await screen.findByLabelText("Imagen del componente:");
    await user.upload(screen.getByLabelText("Imagen del componente:"), file);

    const updateButton = await screen.findByRole("button", { name: /Actualizar componente/i });
    await user.click(updateButton);

    await waitFor(() => {
      expect(addImage).not.toHaveBeenCalled();
      expect(screen.getByText("Por favor, seleccione un archivo de imagen válido.")).toBeInTheDocument();
    });

  });

  it("cancel component update", async () => {

    const user = userEvent.setup();
    
    vi.mocked(getComponent).mockResolvedValue(component);

    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);

    const RouterStub = createRoutesStub([
      { path: "/components/5/update", Component: ComponentsUpdate, loader: updateClientLoader }
    ]);

    render(<RouterStub initialEntries={["/components/5/update"]} />);

    const cancelButton = await screen.findByRole("link", { name: /Cancelar/ });
    await user.click(cancelButton);

    await waitFor(() => {
      expect(updateComponent).not.toHaveBeenCalled();
    });

  });
});