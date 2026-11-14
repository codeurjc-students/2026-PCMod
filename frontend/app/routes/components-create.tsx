import { useNavigate, Link } from "react-router";
import { ArrowLeft, ExclamationCircleFill } from "react-bootstrap-icons";
import ComponentForm from "~/components/componentsForm";
import { useComponentForm } from "~/hooks/use-component-form";
import { createComponent, addImage } from "~/services/components-service";
import type { ComponentFormValues } from "~/utils/component-form-utils";
import { requireAdminUser } from "~/services/route-guard";

export async function clientLoader() {
  return await requireAdminUser();
}

export default function ComponentsCreate() {
  const navigate = useNavigate();

  const initialState: ComponentFormValues = {
    name: "",
    description: "",
    type: "",
    brand: "",
    price: 0,
    stock: 0,
  };

  const form = useComponentForm({
    initialState,
    defaultErrorMessage: "Hubo un error al crear el componente. Por favor, inténtalo de nuevo.",
    submitAction: async (values, image) => {
      const component = await createComponent(values.name, values.description, values.type, values.brand, values.price, values.stock);

      if (image && image.size > 0) {
        await addImage(component.id, image);
      }

      await navigate(`/components/${component.id}`);
    },
  });

  return (
    <main className="main">
      <div className="container">
        <div className="component-form">
          <Link to="/components">
            <ArrowLeft />
            <span className="ms-2">Volver</span>
          </Link>

          <h3 id="create-component-title">
            Ingrese los datos del componente:
          </h3>

          {form.errorMessage && (
            <div className="error-message-text">
              <strong>
                <ExclamationCircleFill /> Error:
              </strong>
              <p id="error-message" className="mb-0 mt-1">
                {form.errorMessage}
              </p>
            </div>
          )}

          <ComponentForm
            onSubmit={form.handleSubmit}
            isPending={form.isPending}
            wasValidated={form.wasValidated}
            componentTypes={form.componentTypes}
            componentTypeLabels={form.componentTypeLabels}
            formRef={form.formRef}
            submitLabel="Guardar componente"
          />
        </div>
      </div>
    </main>
  );
}