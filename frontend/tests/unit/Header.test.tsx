import { render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import "@testing-library/jest-dom";
import Login from "~/routes/login";
import Header from "~/components/header";
import { createRoutesStub } from "react-router";
import userEvent from "@testing-library/user-event";
import { logIn, logOut, reqIsLogged } from "~/services/login-service";
import { useUserStore } from "~/stores/user-store";

vi.mock("~/services/login-service", () => ({
  logIn: vi.fn(),
  logOut: vi.fn(),
  reqIsLogged: vi.fn(),
}));

const userAccount = {
  id: 1,
  name: "User",
  surname: "Example",
  username: "user@example.com",
  address: "User address",
  email: "user@example.com",
  roles: ["REGISTERED_USER"],
};

const adminAccount = {
  id: 2,
  name: "Admin",
  surname: "Example",
  username: "admin@example.com",
  address: "Admin address",
  email: "admin@example.com",
  roles: ["REGISTERED_USER", "ADMIN"],
};

describe("Header", () => {

  beforeEach(() => {
    vi.clearAllMocks();
    useUserStore.setState({ user: null, loginError: null });
    vi.mocked(logIn).mockResolvedValue(undefined);
    vi.mocked(logOut).mockResolvedValue(undefined);
    vi.mocked(reqIsLogged).mockResolvedValue(null as never);
  });

  it("renders the initial header", () => {

    const RouterStub = createRoutesStub([
      { path: "/", Component: Header },
      { path: "/login", Component: Login },
    ]);

    render(<RouterStub initialEntries={["/"]} />)

    expect(screen.getByAltText("Logo")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Componentes" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /Iniciar Sesión/i })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /Registrarse/i })).toBeInTheDocument();

  });

  it("navigates to /login when clicking the login button", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/", Component: Header },
      { path: "/login", Component: Login },
    ]);

    render(<RouterStub initialEntries={["/"]} />)

    await user.click(screen.getByRole("link", { name: /Iniciar Sesión/i }));

    expect(screen.getByRole("heading", { name: "Iniciar sesión:" })).toBeInTheDocument();

  });

  it("shows the profile and logout options for a logged-in user", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/", Component: Header },
      { path: "/login", Component: Login },
    ]);

    vi.mocked(reqIsLogged).mockResolvedValue(userAccount);

    render(<RouterStub initialEntries={["/"]} />);

    await user.click(await screen.findByRole("button", { name: "User" }));

    expect(screen.getByText("Mi perfil")).toBeInTheDocument();
    expect(screen.queryByText("Administración")).not.toBeInTheDocument();
    expect(screen.getByText("Cerrar sesión")).toBeInTheDocument();

  });

  it("shows the administration option for an admin user", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/", Component: Header },
      { path: "/login", Component: Login },
    ]);

    vi.mocked(reqIsLogged).mockResolvedValue(adminAccount);

    render(<RouterStub initialEntries={["/"]} />);

    await user.click(await screen.findByRole("button", { name: "User" }));

    expect(screen.getByText("Mi perfil")).toBeInTheDocument();
    expect(screen.getByText("Administración")).toBeInTheDocument();
    expect(screen.getByText("Cerrar sesión")).toBeInTheDocument();

  });

  it("logs out the user from the dropdown", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/", Component: Header },
      { path: "/login", Component: Login },
    ]);

    vi.mocked(reqIsLogged).mockResolvedValue(userAccount);

    render(<RouterStub initialEntries={["/"]} />);

    await user.click(await screen.findByRole("button", { name: "User" }));
    await user.click(screen.getByText("Cerrar sesión"));

    await waitFor(() => {
      expect(logOut).toHaveBeenCalledTimes(1);
      expect(screen.getByRole("link", { name: /Iniciar Sesión/i })).toBeInTheDocument();
    });
    expect(screen.queryByRole("button", { name: "User" })).not.toBeInTheDocument();
  });

});