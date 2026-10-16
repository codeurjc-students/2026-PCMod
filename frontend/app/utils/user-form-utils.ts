
import { startTransition } from "react";
import type { SubmitEvent } from "react";

export interface UserFormValues {
  name: string;
  surname: string;
  username: string;
  address: string;
  email: string;
  password: string;
}

export function getUserFormValues(formData: FormData): UserFormValues {

  return {
    name: (formData.get("name") as string) ?? "",
    surname: (formData.get("surname") as string) ?? "",
    username: (formData.get("username") as string) ?? "",
    address: (formData.get("address") as string) ?? "",
    email: (formData.get("email") as string) ?? "",
    password: (formData.get("password") as string) ?? "",
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
