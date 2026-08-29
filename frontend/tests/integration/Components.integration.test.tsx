import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import "@testing-library/jest-dom";
import userEvent from "@testing-library/user-event";
import Components, { clientLoader } from "~/routes/components";

describe("ComponentsPageIntegration", () => {

  it("check communication between frontend and backend", async () => {

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
      <Components
        {...({
          loaderData: initialData,
          params: {},
          matches: [],
        } as any)}
      />
    );

    await waitFor(() => {
      const initialComponentsNames = screen.getAllByRole("heading", { level: 3 });
      expect(initialComponentsNames.length).toBe(10);
      expect(initialComponentsNames[0]).toHaveTextContent("AMD Ryzen 7 7800X3D");
    });

    const buttonHasNextTrue = screen.getByRole("button", { name: /Cargar más componentes/i });
    expect(buttonHasNextTrue).toBeInTheDocument();
    await user.click(buttonHasNextTrue);

    await waitFor(() => {
      const updatedComponentsNames = screen.getAllByRole("heading", { level: 3 });
      expect(updatedComponentsNames.length).toBe(11);
      expect(updatedComponentsNames[10]).toHaveTextContent("Kingston FURY Beast");
    });

    const buttonHasNextFalse = screen.queryByRole("button", { name: /Cargar más componentes/i });
    expect(buttonHasNextFalse).not.toBeInTheDocument();

  });

});