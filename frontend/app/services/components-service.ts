import type ComponentDTO from "~/dtos/ComponentDTO";
import type ImageDTO from "~/dtos/ImageDTO";

const API_URL = "/api/v1/components";
const PAGE_SIZE = 10;

export type ComponentsPageResult = {
  items: ComponentDTO[];
  hasNext: boolean;
};

function getBaseUrl(): string {
  const baseUrl = typeof window !== "undefined" && window.location.origin
    ? window.location.origin
    : "https://localhost:443";
  return baseUrl;
}

export async function getComponents(page: number): Promise<ComponentsPageResult> {

  const baseUrl = getBaseUrl();

  const url = new URL(`${API_URL}/?page=${page}&size=${PAGE_SIZE}`, baseUrl);

  const res = await fetch(url.toString());

  if (!res.ok) {
    throw new Error("Error fetching components");
  }

  const data = await res.json();

  if (Array.isArray(data?.content)) {
    return { items: data.content, hasNext: !data.last };
  }

  return { items: [], hasNext: false };
}

export async function getRecentComponents(): Promise<ComponentDTO[]> {

  const baseUrl = getBaseUrl();

  const url = new URL(`${API_URL}/recent`, baseUrl);

  const res = await fetch(url.toString());

  if (!res.ok) {
    throw new Error("Error fetching recent components");
  }

  const data = await res.json();

  if (Array.isArray(data)) {
    return data;
  }

  return [];
}

export async function getComponent(id: number): Promise<ComponentDTO> {

  const baseUrl = getBaseUrl();

  const url = new URL(`${API_URL}/${id}`, baseUrl);

  const res = await fetch(url.toString());

  if (!res.ok) {
    throw new Error("Error fetching component");
  }

  return await res.json();
}

export async function createComponent (name: string, description: string, type: string, brand: string, price: number, stock: number): Promise<ComponentDTO> {

  const url = new URL(`${API_URL}/`, getBaseUrl());
  const res = await fetch(url.toString(), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      name,
      description,
      type,
      brand,
      price,
      stock
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

    throw new Error(message ?? "There was an error while creating the component.");
  }

  return await res.json();
}

export async function addImage(id: number, image: File): Promise<ImageDTO> {

  const formData = new FormData();
  formData.append('imageFile', image);

  const url = new URL(`${API_URL}/${id}/image`, getBaseUrl());
  const res = await fetch(url.toString(), {
    method: 'POST',
    body: formData
  });

  if (!res.ok) {
    throw new Error("Hubo un error al subir la imagen. Solo se permiten imágenes JPEG, PNG y WebP.");
  }

  return await res.json();
}

export async function deleteComponent(id: number): Promise<void> {

  const url = new URL(`${API_URL}/${id}`, getBaseUrl());
  const response = await fetch(url.toString(), {
    method: "DELETE",
  });

  if (!response.ok) {
    throw new Error("Error deleting component");
  }

}

export async function updateComponent(id: number, name: string, description: string, type: string, brand: string, price: number, stock: number): Promise<ComponentDTO> {
  const url = new URL(`${API_URL}/${id}`, getBaseUrl());
  const res = await fetch(url.toString(), {
    method: 'PUT',
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      name,
      description,
      type,
      brand,
      price,
      stock
    })
  });

  if (!res.ok) {
    let message: string | undefined;

    try {
      const error = await res.json();
      message = typeof error.message === "string" ? error.message : undefined;
    } catch {
      message = undefined;
    }

    throw new Error(message ?? "Hubo un error al actualizar el componente.");
  }

  return await res.json();
}

export async function getComponentTypes(): Promise<string[]> {
  const url = new URL("/api/v1/component-types", getBaseUrl());
  const response = await fetch(url.toString());

  if (!response.ok) {
    throw new Error("No se pudieron obtener los tipos de componentes");
  }

  return await response.json();
}