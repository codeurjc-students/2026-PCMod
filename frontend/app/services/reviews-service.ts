import type ReviewDTO from "~/dtos/ReviewDTO";

const API_URL = "/api/v1/components";
const PAGE_SIZE = 10;

export type ReviewsResult = {
  items: ReviewDTO[];
  hasNext: boolean;
};

function getBaseUrl(): string {
  const baseUrl = typeof window !== "undefined" && window.location.origin
    ? window.location.origin
    : "https://localhost:443";
  return baseUrl;
}

export async function getReviews(id: number, page: number): Promise<ReviewsResult> {

  const baseUrl = getBaseUrl();

  const url = new URL(`${API_URL}/${id}/reviews/?page=${page}&size=${PAGE_SIZE}`, baseUrl);

  const res = await fetch(url.toString());

  if (!res.ok) {
    throw new Error("Error fetching reviews");
  }

  const data = await res.json();

  if (Array.isArray(data?.content)) {
    return { items: data.content, hasNext: !data.last };
  }

  return { items: [], hasNext: false };
}

export async function deleteReview(id: number, reviewId: number): Promise<void> {

  const url = new URL(`${API_URL}/${id}/reviews/${reviewId}`, getBaseUrl());
  const res = await fetch(url.toString(), {
    method: "DELETE",
  });

  if (!res.ok) {
    throw new Error("Error deleting review");
  }

}