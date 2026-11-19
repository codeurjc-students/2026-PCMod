import type OrderDTO from "~/dtos/OrderDTO";

const API_URL = "/api/v1/orders";

function getBaseUrl(): string {
  const baseUrl = typeof window !== "undefined" && window.location.origin
    ? window.location.origin
    : "https://localhost:443";
  return baseUrl;
}

export async function getOrder(id: number): Promise<OrderDTO> {

  const baseUrl = getBaseUrl();

  const url = new URL(`${API_URL}/${id}`, baseUrl);

  const res = await fetch(url.toString());

  if (!res.ok) {
    throw new Error("Hubo un error al cargar el pedido.");
  }

  return await res.json();
}