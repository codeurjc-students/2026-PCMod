import { useActionState, useEffect, useRef, useState } from "react";
import type { SubmitEvent } from "react";
import { getComponentTypes } from "~/services/components-service";
import { getComponentFormValues } from "~/utils/component-form-utils";
import type { ComponentFormValues } from "~/utils/component-form-utils";
import { validateImage, handleFormSubmit } from "~/utils/general-form-utils";

interface UseComponentFormProps {
  initialState: ComponentFormValues;
  submitAction: (
    values: ComponentFormValues,
    image: File | null
  ) => Promise<void>;
  defaultErrorMessage: string;
}

const componentTypeLabels: Record<string, string> = {
  CPU: "Procesador",
  GPU: "Tarjeta gráfica",
  RAM: "Memoria RAM",
  STORAGE: "Almacenamiento",
  MOTHERBOARD: "Placa base",
  PSU: "Fuente de alimentación",
  CASE: "Caja",
};

export function useComponentForm({ initialState, submitAction, defaultErrorMessage }: UseComponentFormProps) {
  const [wasValidated, setWasValidated] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [componentTypes, setComponentTypes] = useState<string[]>([]);

  const formRef = useRef<HTMLFormElement>(null);

  async function action(_prevState: ComponentFormValues, formData: FormData): Promise<ComponentFormValues> {
    const values = getComponentFormValues(formData);
    const image = formData.get("image") as File | null;

    const imageError = validateImage(image);

    if (imageError) {
      setErrorMessage(imageError);
      return values;
    }

    try {
      setErrorMessage("");
      await submitAction(values, image);
    } catch (error) {
      console.error(error);

      setErrorMessage(
        error instanceof Error
          ? error.message
          : defaultErrorMessage
      );
    }

    return values;
  }

  const [, formAction, isPending] = useActionState(
    action,
    initialState
  );

  useEffect(() => {
    async function loadComponentTypes() {
      try {
        const types = await getComponentTypes();
        setComponentTypes(types);
      } catch (error) {
        setErrorMessage(
          error instanceof Error
            ? error.message
            : "Error al cargar los tipos de componentes."
        );
      }
    }

    void loadComponentTypes();
  }, []);

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    handleFormSubmit(event, formAction, setWasValidated);
  }

  return {
    wasValidated,
    errorMessage,
    componentTypes,
    componentTypeLabels,
    formRef,
    isPending,
    handleSubmit,
  };
}
