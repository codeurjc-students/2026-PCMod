import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import { getComponents } from "~/services/components-service";
import Components, { clientLoader } from "~/routes/components";
import { createRoutesStub } from "react-router";

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

    const items = screen.getAllByRole("listitem");
    expect(items).toHaveLength(2);

  });
});