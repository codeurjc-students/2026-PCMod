import type ComponentDTO from "~/dtos/ComponentDTO";

const API_URL = "/api/v1/components";
const PAGE_SIZE = 10;

export type ComponentsPageResult = {
  items: ComponentDTO[];
  hasNext: boolean;
};

export async function getComponents(page: number): Promise<ComponentsPageResult> {
  const res = await fetch(`${API_URL}/?page=${page}&size=${PAGE_SIZE}`);
  if (!res.ok) {
    throw new Error("Error fetching components");
  }

  const data = await res.json();

  if (Array.isArray(data?.content)) {
    return { items: data.content, hasNext: !data.last };
  }

  return { items: [], hasNext: false };
}