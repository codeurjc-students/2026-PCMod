import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import Profile, { clientLoader } from "~/routes/profile";
import { createRoutesStub } from "react-router";
import userEvent from "@testing-library/user-event";
import { deleteUser, getUser, logOut, reqIsLogged } from "~/services/users-service";

const userAccount = {
  id: 1,
  name: "User",
  surname: "Example",
  username: "user@example.com",
  address: "User address",
  email: "user@example.com",
  roles: ["REGISTERED_USER"],
};

vi.mock("~/services/users-service", () => ({
  getUser: vi.fn(),
  logIn: vi.fn(),
  logOut: vi.fn(),
  reqIsLogged: vi.fn(),
  deleteUser: vi.fn(),
}));

describe("ProfilePage", () => {

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders the profile page", async () => {

    const RouterStub = createRoutesStub([
      { path: "/me", Component: Profile, loader: () => userAccount },
    ]);

    render(<RouterStub initialEntries={["/me"]} />);

    await waitFor(() => {
      expect(screen.getByText("Nombre:")).toBeInTheDocument();
      expect(screen.getByText("Apellidos:")).toBeInTheDocument();
      expect(screen.getByText("Nombre de usuario:")).toBeInTheDocument();
      expect(screen.getByText("Dirección:")).toBeInTheDocument();
      expect(screen.getByText("Correo electrónico:")).toBeInTheDocument();
      expect(screen.getByRole("button", { name: "Borrar cuenta" })).toBeInTheDocument();
      expect(screen.getByRole("img", { name: "UserImage" })).toBeInTheDocument();
      expect(screen.getByRole("heading", { level: 2, name: "Mis compras:" })).toBeInTheDocument();
    });

  });

  it("clientLoader returns the user", async () => {
    vi.mocked(getUser).mockResolvedValue(userAccount);

    const result = await clientLoader();

    expect(getUser).toHaveBeenCalledTimes(1);
    expect(result).toEqual(userAccount);
  });

  it("deletes user account", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/me", Component: Profile, loader: () => userAccount },
      { path: "/", Component: () => <h1>HomePage</h1> }
    ]);

    vi.mocked(reqIsLogged).mockResolvedValue(userAccount);
    vi.mocked(deleteUser).mockResolvedValue();
    vi.mocked(logOut).mockResolvedValue();

    render(<RouterStub initialEntries={["/me"]} />);

    await user.click(await screen.findByRole("button", { name: "Borrar cuenta" }));

    expect(await screen.findByText(
      "¿Está seguro de que desea borrar su cuenta?",
    )).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Cancelar/ })).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Eliminar cuenta/ })).toBeInTheDocument();

    await user.click(await screen.findByRole("button", { name: /Eliminar cuenta/ }));

    await waitFor(() => {
      expect(deleteUser).toHaveBeenCalledWith(userAccount.id);
      expect(logOut).toHaveBeenCalledTimes(1);
      expect(screen.getByRole("heading", { name: "HomePage" })).toBeInTheDocument();
    });

  });

  it("cancel user account deletion", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/me", Component: Profile, loader: () => userAccount }
    ]);

    vi.mocked(reqIsLogged).mockResolvedValue(userAccount);

    render(<RouterStub initialEntries={["/me"]} />);

    await user.click(await screen.findByRole("button", { name: "Borrar cuenta" }));

    expect(await screen.findByText(
      "¿Está seguro de que desea borrar su cuenta?",
    )).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Cancelar/ })).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Eliminar cuenta/ })).toBeInTheDocument();

    await user.click(await screen.findByRole("button", { name: /Cancelar/ }));

    await waitFor(() => {
      expect(deleteUser).not.toHaveBeenCalled();
      expect(logOut).not.toHaveBeenCalled();
    });

  });

  it("closes user account deletion modal with the close button", async () => {
    const user = userEvent.setup();
    const RouterStub = createRoutesStub([
      { path: "/me", Component: Profile, loader: () => userAccount }
    ]);

    vi.mocked(reqIsLogged).mockResolvedValue(userAccount);

    render(<RouterStub initialEntries={["/me"]} />);

    await user.click(await screen.findByRole("button", { name: "Borrar cuenta" }));

    expect(await screen.findByText(
      "¿Está seguro de que desea borrar su cuenta?",
    )).toBeInTheDocument();

    await user.click(await screen.findByRole("button", { name: "Close" }));

    await waitFor(() => {
      expect(screen.queryByText(
        "¿Está seguro de que desea borrar su cuenta?",
      )).not.toBeInTheDocument();
      expect(deleteUser).not.toHaveBeenCalled();
      expect(logOut).not.toHaveBeenCalled();
    });
  });

});