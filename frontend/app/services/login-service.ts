import type UserDTO from "~/dtos/UserDTO";

const API_USERS_URL = "/api/v1/users";
const API_AUTH_URL = "/api/v1/auth";

function getBaseUrl(): string {
  const baseUrl = typeof window !== "undefined" && window.location.origin
    ? window.location.origin
    : "https://localhost:443";
  return baseUrl;
}

export class HttpError extends Error {

  status: number;

  constructor(status: number, message?: string) {
    super(message ?? `HTTP ${status}`);
    this.status = status;
  }

}

export async function reqIsLogged(): Promise<UserDTO> {

  const url = new URL(`${API_USERS_URL}/me`, getBaseUrl());
  const res = await fetch(url.toString());

  if (!res.ok) {
    throw new HttpError(res.status);
  }

  return await res.json();
}

export async function logIn(email: string, pass: string): Promise<void> {

  const url = new URL(`${API_AUTH_URL}/login`, getBaseUrl());
  const res = await fetch(url.toString(), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username: email, password: pass }),
  });

  if (!res.ok) {
    throw new Error("There was an error while logging in.");
  }

}

export async function logOut(): Promise<void> {

  const url = new URL(`${API_AUTH_URL}/logout`, getBaseUrl());
  const res = await fetch(url.toString(), {
    method: "POST",
  });

  if (!res.ok) {
    throw new Error("There was an error while logging out.");
  }

}