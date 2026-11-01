import { startTransition } from "react";
import type { SubmitEvent } from "react";

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
