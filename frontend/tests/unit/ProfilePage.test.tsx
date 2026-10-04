import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import Profile, { clientLoader } from "~/routes/profile";
import { createRoutesStub } from "react-router";
import { getUser } from "~/services/users-service";

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
  reqIsLogged: vi.fn(),
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

});