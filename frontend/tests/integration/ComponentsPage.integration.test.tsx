import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router";
import Components, { clientLoader } from "~/routes/components";

describe("ComponentsPageIntegration", () => {

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

});