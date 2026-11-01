import { useActionState, useEffect, useRef, useState } from "react";
import type { SubmitEvent } from "react";
import ComponentForm from "~/components/componentsForm";
import { ArrowLeft, ExclamationCircleFill } from "react-bootstrap-icons";
import { Link, useNavigate } from "react-router";
import { createComponent, addImage, getComponentTypes } from "~/services/components-service";
import { getComponentFormValues } from "~/utils/component-form-utils";
import type { ComponentFormValues } from "~/utils/component-form-utils";
import { validateImage, handleFormSubmit } from "~/utils/general-form-utils";
import { requireAdminUser } from "~/services/route-guard";

export async function clientLoader() {
    return await requireAdminUser();
}

export default function ComponentsCreate() {

  const navigate = useNavigate();
  const [wasValidated, setWasValidated] = useState(false);
  const formRef = useRef<HTMLFormElement>(null);
  const [errorMessage, setErrorMessage] = useState("");
  const [, formAction, isPending] = useActionState(
    createComponentAction,
    { name: "", description: "", type: "", brand: "", price: 0.00, stock: 0 },
  );
  const [componentTypes, setComponentTypes] = useState<string[]>([]);

  const componentTypeLabels: Record<string, string> = {
    CPU: "Procesador",
    GPU: "Tarjeta gráfica",
    RAM: "Memoria RAM",
    STORAGE: "Almacenamiento",
    MOTHERBOARD: "Placa base",
    PSU: "Fuente de alimentación",
    CASE: "Caja"
  };

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

  async function createComponentAction(_prevState: ComponentFormValues, formData: FormData) {
    const { name, description, type, brand, price, stock } = getComponentFormValues(formData);
    const image = formData.get("image") as File | null;

    const imageError = validateImage(image);
    if (imageError) {
      setErrorMessage(imageError);
      return { name, description, type, brand, price, stock };
    }

    try {
      const component = await createComponent(name, description, type, brand, price, stock);

      if (image && image.size > 0) {
        await addImage(component.id, image);
      }

      await navigate(`/components/${component.id}`);
    } catch (error) {
      console.log(error);

      setErrorMessage(
        error instanceof Error
          ? error.message
          : "Hubo un error al crear el componente. Por favor, inténtalo de nuevo.",
      );
    }

    return { name, description, type, brand, price, stock };
  }

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    handleFormSubmit(event, formAction, setWasValidated);
  }

  return (
    <main className="main">
      <div className="container">
        <div className="component-form">
          <Link to="/">
            <ArrowLeft />
            <span className="ms-2">Volver</span>
          </Link>

          <h3 id="create-component-title">
            Ingrese los datos del componente:
          </h3>

          {errorMessage && (
            <div className="error-message-text">
              <strong>
                <ExclamationCircleFill /> Error:
              </strong>
              <p id="error-message" className="mb-0 mt-1">
                {errorMessage}
              </p>
            </div>
          )}

          <ComponentForm
            onSubmit={handleSubmit}
            isPending={isPending}
            wasValidated={wasValidated}
            componentTypes={componentTypes}
            componentTypeLabels={componentTypeLabels}
            formRef={formRef}
          />
        </div>
      </div>
    </main>
  );
}