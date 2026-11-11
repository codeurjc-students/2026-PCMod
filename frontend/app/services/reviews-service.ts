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

export async function createReview (id: number, title: string, message:string, rating: number): Promise<ReviewDTO> {

  const url = new URL(`${API_URL}/${id}/reviews/`, getBaseUrl());
  const res = await fetch(url.toString(), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      title,
      message,
      rating
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

    throw new Error(message ?? "There was an error while creating the review.");
  }

  return await res.json();
}

export async function hasUserReviewed(id: number): Promise<boolean> {

  const url = new URL(`${API_URL}/${id}/reviews/has-reviewed`, getBaseUrl());
  const res = await fetch(url.toString());

  if (!res.ok) {
    let message: string | undefined;

    try {
      const error = await res.json();
      message = typeof error.message === "string" ? error.message : undefined;
    } catch {
      message = undefined;
    }

    throw new Error(message ?? "There was an error while checking user review availability.");
  }

  return await res.json();
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