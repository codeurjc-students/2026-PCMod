export interface ReviewFormValues {
  title: string;
  message: string;
  rating: number;
}

export function getReviewFormValues(formData: FormData): ReviewFormValues {

  return {
    title: (formData.get("title") as string) ?? "",
    message: (formData.get("message") as string) ?? "",
    rating: Number((formData.get("rating") as string) ?? 0),
  };

}
