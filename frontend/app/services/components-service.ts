import type ComponentDTO from "~/dtos/ComponentDTO";

const API_URL = "/api/v1/components";
const PAGE_SIZE = 10;

export type ComponentsPageResult = {
  items: ComponentDTO[];
  hasNext: boolean;
};

export async function getComponents(page: number): Promise<ComponentsPageResult> {

  const baseUrl = typeof window !== "undefined" && window.location.origin
    ? window.location.origin
    : "https://localhost:443";

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