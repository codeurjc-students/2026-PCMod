import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import { logIn, reqIsLogged } from "~/services/login-service";
import Login from "~/routes/login";
import { createRoutesStub } from "react-router";
import userEvent from "@testing-library/user-event";

vi.mock("~/services/login-service", () => ({
  logIn: vi.fn(),
  reqIsLogged: vi.fn(),
}));

describe("LoginPage", () => {

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders the login form", async () => {

    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    expect(screen.getByRole("heading", { name: "Iniciar sesión:" })).toBeInTheDocument();
    expect(screen.getByLabelText("Correo electrónico:")).toBeInTheDocument();
    expect(screen.getByLabelText("Contraseña:")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Iniciar sesión/i })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Haga click aquí para crear una." })).toBeInTheDocument();

  });

  it("validates empty credentials", async () => {
    const user = userEvent.setup();

    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    const loginButton = screen.getByRole("button", { name: /Iniciar sesión/i });

    await user.click(loginButton);

    expect(screen.getByText("Por favor, ingrese un correo electrónico válido.")).toBeInTheDocument();
    expect(screen.getByText("Contraseña incorrecta. Inténtelo de nuevo.")).toBeInTheDocument();
    expect(logIn).not.toHaveBeenCalled();

  });

  it("log in a user with correct credentials", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
      { path: "/", Component: () => <h1>HomePage</h1> },
    ]);

    vi.mocked(logIn).mockResolvedValue(undefined);
    vi.mocked(reqIsLogged).mockResolvedValue({} as never);
    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "HomePage" })).toBeInTheDocument();
    });
    expect(logIn).toHaveBeenCalledWith("user@example.com", "userpass");

  });

  it("rejects login with invalid email format", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user");
    await user.type(screen.getByLabelText("Contraseña:"), "userpass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    expect(screen.getByText("Por favor, ingrese un correo electrónico válido.")).toBeInTheDocument();
    expect(logIn).not.toHaveBeenCalled();

  });

  it("rejects login with invalid password format", async () => {

    const user = userEvent.setup();
    const checkValiditySpy = vi.spyOn(HTMLFormElement.prototype, "checkValidity").mockReturnValue(false);
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
    ]);

    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "user@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "pass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    expect(screen.getByText("Contraseña incorrecta. Inténtelo de nuevo.")).toBeInTheDocument();
    expect(logIn).not.toHaveBeenCalled();
    checkValiditySpy.mockRestore();

  });

  it("displays error message if invalid credentials are provided", async () => {

    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/login", Component: Login },
    ]);

    vi.mocked(logIn).mockRejectedValue(new Error("Invalid credentials"));
    render(<RouterStub initialEntries={["/login"]} />);

    await user.type(screen.getByLabelText("Correo electrónico:"), "wrongUser@example.com");
    await user.type(screen.getByLabelText("Contraseña:"), "wrongPass");
    await user.click(screen.getByRole("button", { name: /Iniciar sesión/i }));

    await waitFor(() => {
      expect(screen.getByText("Error al iniciar sesión. Por favor, inténtenlo de nuevo")).toBeInTheDocument();
    });
    expect(logIn).toHaveBeenCalledWith("wrongUser@example.com", "wrongPass");

  });
});