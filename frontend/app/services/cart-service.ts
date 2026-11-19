import type CartDTO from "~/dtos/CartDTO";

const API_URL = "/api/v1/cart";

function getBaseUrl(): string {
  const baseUrl = typeof window !== "undefined" && window.location.origin
    ? window.location.origin
    : "https://localhost:443";
  return baseUrl;
}

export async function getCart(): Promise<CartDTO> {

  const baseUrl = getBaseUrl();

  const url = new URL(`${API_URL}/`, baseUrl);

  const res = await fetch(url.toString());

  if (!res.ok) {
    throw new Error("Hubo un error al cargar el carrito.");
  }

  return await res.json();
}