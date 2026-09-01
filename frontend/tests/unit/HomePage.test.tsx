import "@testing-library/jest-dom";
import { render, waitFor, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { createRoutesStub } from "react-router";
import { beforeEach, describe, expect, it, vi } from "vitest";
import Index from "~/routes/index";
import Components from "~/routes/components";
import { getRecentComponents } from "~/services/components-service";
import type ComponentDTO from "~/dtos/ComponentDTO";


vi.mock("~/services/components-service", () => ({
  getRecentComponents: vi.fn(),
}));

describe("HomePage", () => {

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders home page correctly", async () => {

    const sampleMockData = [
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
      {
        id: 3,
        name: "Kingston NV3",
        description:
          "Capacidad: 1 TB. Factor de forma de disco SSD: M.2. Interfaz: PCI Express 4.0. NVMe: Si. Tipo de memoria: 3D NAND",
        type: "STORAGE",
        brand: "Kingston",
        price: 145.95,
        stock: 15,
      },
    ];

    vi.mocked(getRecentComponents).mockResolvedValue(sampleMockData);

    const RouterStub = createRoutesStub([
      {
        path: "/",
        Component: Index,
        loader: async () => ({
          recentComponents: await getRecentComponents(),
        }),
      },
    ]);

    render(<RouterStub initialEntries={["/"]} />);

    await waitFor(() => {
      expect(screen.getByText("Bienvenido a PCMod")).toBeInTheDocument();
      expect(screen.getByAltText("PCMod Logo")).toBeInTheDocument();
    });

    expect(vi.mocked(getRecentComponents)).toHaveBeenCalledTimes(1);

    await waitFor(() => {
      expect(screen.getByText("AMD Ryzen 7 7800X3D")).toBeInTheDocument();
      expect(screen.getByText("NVIDIA GeForce RTX 5060 WINDFORCE MAX OC")).toBeInTheDocument();
      expect(screen.getByText("Kingston NV3")).toBeInTheDocument();
    });

    const homeButton = await screen.findByRole("button", { name: /Ver todos los componentes/i });
    expect(homeButton).toBeInTheDocument();

  });

  it("renders home page correctly without recent components", async () => {

    const sampleMockData: ComponentDTO[] = [];

    vi.mocked(getRecentComponents).mockResolvedValue(sampleMockData);

    const RouterStub = createRoutesStub([
      {
        path: "/",
        Component: Index,
        loader: async () => ({
          recentComponents: await getRecentComponents(),
        }),
      },
    ]);

    render(<RouterStub initialEntries={["/"]} />);

    await waitFor(() => {
      expect(screen.getByText("Bienvenido a PCMod")).toBeInTheDocument();
      expect(screen.getByAltText("PCMod Logo")).toBeInTheDocument();
    });

    expect(vi.mocked(getRecentComponents)).toHaveBeenCalledTimes(1);

    await waitFor(() => {
      expect(screen.getByText("No se han encontrado componentes de PC.")).toBeInTheDocument();
    });

    expect(screen.queryByRole("button", { name: /Ver todos los componentes/i })).not.toBeInTheDocument();

  });

  it("navigates to /components when clicking the button", async () => {

    const user = userEvent.setup();

    const sampleMockData = [
      {
        id: 1,
        name: "AMD Ryzen 7 7800X3D",
        description: "Procesador gaming de alto rendimiento.",
        type: "CPU",
        brand: "AMD",
        price: 350.0,
        stock: 5,
      },
    ];

    const RouterStub = createRoutesStub([
      {
        path: "/",
        Component: Index,
        loader: async () => ({ recentComponents: sampleMockData }),
      },
      {
        path: "/components",
        Component: Components,
        loader: async () => ({ items: [], hasNext: false }),
      },
    ]);

    render(<RouterStub initialEntries={["/"]} />);

    const homeButton = await screen.findByRole("button", { name: /Ver todos los componentes/i });
    await user.click(homeButton);

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: /Componentes:/i })).toBeInTheDocument();
    });

  });

});