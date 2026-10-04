import { render, screen, waitFor } from "@testing-library/react";
import fetchCookie from "fetch-cookie";
import userEvent from "@testing-library/user-event";
import { createRoutesStub, Navigate } from "react-router";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import "@testing-library/jest-dom";
import { CookieJar } from "tough-cookie";
import Login from "~/routes/login";
import Profile, { clientLoader } from "~/routes/profile";

describe("ProfileIntegration", () => {

  beforeEach(() => {
    vi.stubGlobal("fetch", fetchCookie(globalThis.fetch, new CookieJar()));
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("renders the profile page with the correct data", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <Navigate to="/me" replace /> },
      { path: "/me", Component: Profile, loader: clientLoader },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(screen.getByText("user")).toBeInTheDocument();
      expect(screen.getByText("example")).toBeInTheDocument();
      expect(screen.getByText("user_example")).toBeInTheDocument();
      expect(screen.getByText("c/example_address 1")).toBeInTheDocument();
      expect(screen.getByText("user@example.com")).toBeInTheDocument();
    });
  });

});