import type ImageDTO from "~/dtos/ImageDTO";
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

export async function register(name: string, surname: string, username: string, address: string, email: string, password: string): Promise<UserDTO> {

  const url = new URL(`${API_AUTH_URL}/register`, getBaseUrl());
  const res = await fetch(url.toString(), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      name,
      surname,
      username,
      address,
      email,
      password
    }),
  });

  if (!res.ok) {
    let message: string | undefined;

    try {
      const error = await res.json();
      message = typeof error.message === "string" ? error.message : undefined;
    } catch {
      message = undefined;
    }

    throw new Error(message ?? "There was an error while registering.");
  }

  return await res.json();

}

export async function addImage(id: number, image: File): Promise<ImageDTO> {

  const formData = new FormData();
  formData.append('imageFile', image);

  const res = await fetch(`${API_USERS_URL}/${id}/image`, {
    method: 'POST',
    body: formData
  });

  if (!res.ok) {
    throw new Error("Hubo un error al subir la imagen. Solo se permiten imágenes JPEG, PNG y WebP.");
  }

  return await res.json();

}