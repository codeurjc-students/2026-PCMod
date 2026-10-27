import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import "@testing-library/jest-dom";
import Index, { clientLoader } from "~/routes/index";
import { MemoryRouter } from "react-router";

describe("HomePageIntegration", () => {

  it("check home page data rendering", async () => {

    const initialData = await clientLoader();

    render(
      <MemoryRouter>
        <Index
          {...({
            loaderData: initialData,
            params: {},
            matches: [],
          } as any)}
        />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText("Bienvenido a PCMod")).toBeInTheDocument();
      expect(screen.getByAltText("PCMod Logo")).toBeInTheDocument();
    });

    await waitFor(() => {
      expect(screen.getByText("Seagate BarraCuda 3.5")).toBeInTheDocument();
      expect(screen.getByText("Kingston FURY Beast")).toBeInTheDocument();
      expect(screen.getByText("Corsair iCUE LINK 3500X RGB")).toBeInTheDocument();
    });

    expect(screen.queryByRole("button", { name: /Ver todos los componentes/i })).toBeInTheDocument();

  });

});