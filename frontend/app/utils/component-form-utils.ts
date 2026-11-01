import { startTransition } from "react";
import type { SubmitEvent } from "react";

export interface ComponentFormValues {
  name: string;
  description: string;
  type: string;
  brand: string;
  price: number;
  stock: number;
}

export function getComponentFormValues(formData: FormData): ComponentFormValues {

  return {
    name: (formData.get("name") as string) ?? "",
    description: (formData.get("description") as string) ?? "",
    type: (formData.get("type") as string) ?? "",
    brand: (formData.get("brand") as string) ?? "",
    price: Number((formData.get("price") as string) ?? 0.00),
    stock: Number((formData.get("stock") as string) ?? 0),
  };

}

export function validateImage(image: File | null): string | null {

  if (!image || image.size === 0) {
    return null;
  }

  const allowedImageTypes = [
    "image/jpeg",
    "image/png",
    "image/webp",
  ];

  const maxImageSize = 10 * 1024 * 1024;

  if (image.size > maxImageSize || !allowedImageTypes.includes(image.type)) {
    return "Hubo un error al subir la imagen. Solo se permiten imágenes JPEG, PNG y WebP.";
  }

  return null;
}

export function handleFormSubmit(event: SubmitEvent<HTMLFormElement>, formAction: (formData: FormData) => void, setWasValidated: (value: boolean) => void) {

  event.preventDefault();

  const form = event.currentTarget;

  setWasValidated(true);

  if (!form.checkValidity()) {
    event.stopPropagation();
    return;
  }

  startTransition(() => {
    formAction(new FormData(form));
  });

}
