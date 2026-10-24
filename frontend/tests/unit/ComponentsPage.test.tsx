import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import { getComponents } from "~/services/components-service";
import Components, { clientLoader } from "~/routes/components";
import { MemoryRouter, createRoutesStub } from "react-router";

vi.mock("~/services/components-service", () => ({
  getComponents: vi.fn(),
}));

describe("ComponentsPage", () => {

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders all components", async () => {
    const sampleMockData = {
      items: [
        {
          id: 1,
          name: "AMD Ryzen 7 7800X3D",
          description:
            "8 núcleos y 16 hilos ideales para gaming. 96 MB de caché L3 3D V-Cache, baja latencia. Compatible con memorias DDR5, doble canal. Frecuencia base 4.2 GHz y turbo hasta 5.0 GHz",
          type: "CPU",
          brand: "AMD",
          price: 350.0,
          stock: 5,
        },
        {
          id: 2,
          name: "NVIDIA GeForce RTX 5060 WINDFORCE MAX OC",
          description:
            "Ray Tracing y DLSS 4 para gaming fluido. 8GB GDDR7 y arquitectura Blackwell. WINDFORCE 2X: refrigeración eficiente y silenciosa. DisplayPort 2.1 y HDMI 2.1b hasta 8K. Tensor Cores 5ª gen y Reflex 2 para IA avanzada",
          type: "GPU",
          brand: "Gigabyte",
          price: 359.85,
          stock: 2,
        },
      ],
      hasNext: false,
    };

    vi.mocked(getComponents).mockResolvedValue(sampleMockData);

    const RouterStub = createRoutesStub([
      {
        path: "/components",
        Component: Components,
        loader: async ({ request }) => {
          const url = new URL(request.url);
          const page = Number(url.searchParams.get("page") ?? 0);
          const result = await getComponents(page);
          return { items: result.items, hasNext: result.hasNext };
        },
      },
    ]);

    render(<RouterStub initialEntries={["/components?page=0"]} />);

    expect(vi.mocked(getComponents)).toHaveBeenCalledTimes(1);

    await waitFor(() => {
      expect(screen.getByText("AMD Ryzen 7 7800X3D")).toBeInTheDocument();
      expect(screen.getByText("NVIDIA GeForce RTX 5060 WINDFORCE MAX OC")).toBeInTheDocument();
    });

    const items = screen.getAllByRole("heading", { level: 3 });
    expect(items).toHaveLength(2);

  });

  it("renders an empty state when there are no components", async () => {
    vi.mocked(getComponents).mockResolvedValue({
      items: [],
      hasNext: false,
    });

    const RouterStub = createRoutesStub([
      {
        path: "/components",
        Component: Components,
        loader: async ({ request }) => {
          const url = new URL(request.url);
          const page = Number(url.searchParams.get("page") ?? 0);
          const result = await getComponents(page);
          return { items: result.items, hasNext: result.hasNext };
        },
      },
    ]);

    render(<RouterStub initialEntries={["/components?page=0"]} />);

    expect(vi.mocked(getComponents)).toHaveBeenCalledTimes(1);

    await waitFor(() => {
      expect(screen.getByText("No se han encontrado componentes de PC.")).toBeInTheDocument();
    });

    expect(screen.queryAllByRole("heading", { level: 3 })).toHaveLength(0);
    expect(screen.queryByRole("button", { name: /Cargar más componentes/i })).not.toBeInTheDocument();
  });

  it("clientLoader returns the correct data", async () => {
    const sampleMockData = {
      items: [],
      hasNext: false,
    };
    vi.mocked(getComponents).mockResolvedValue(sampleMockData);

    const result = await clientLoader({
      request: new Request("http://localhost/components?page=0"),
    } as Parameters<typeof clientLoader>[0]);

    expect(getComponents).toHaveBeenCalledTimes(1);
    expect(result).toEqual(sampleMockData);
  });

  it("checks handleLoadMore functionality", async () => {
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

    const nextComponent = {
      id: 2,
      name: "NVIDIA GeForce RTX 5060",
      description: "Tarjeta gráfica para gaming.",
      type: "GPU",
      brand: "NVIDIA",
      price: 359.85,
      stock: 2,
    };

    vi.mocked(getComponents).mockResolvedValue({
      items: [nextComponent],
      hasNext: false,
    });

    render(
      <MemoryRouter>
        <Components
          loaderData={{
            items: [firstComponent],
            hasNext: true,
          }}
        />
      </MemoryRouter>,
    );

    const loadMoreButton = screen.getByRole("button", { name: /Cargar más componentes/i });
    await user.click(loadMoreButton);

    await waitFor(() => {
      expect(getComponents).toHaveBeenCalledWith(1);
      expect(screen.getByText("NVIDIA GeForce RTX 5060")).toBeInTheDocument();
    });

    expect(screen.getAllByRole("heading", { level: 3 })).toHaveLength(2);
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

    vi.mocked(getComponents).mockRejectedValueOnce(new Error("Network error"));

    render(
      <MemoryRouter>
        <Components
          loaderData={{
            items: [firstComponent],
            hasNext: true,
          }}
        />
      </MemoryRouter>,
    );

    await user.click(screen.getByRole("button", { name: /Cargar más componentes/i }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent(
        "Hubo un problema al cargar más componentes. Por favor, pruebe de nuevo.",
      );
    });

    expect(getComponents).toHaveBeenCalledWith(1);
    expect(screen.getByRole("button", { name: /Cargar más componentes/i })).toBeInTheDocument();
  });
});