import { useNavigate, Link } from "react-router";
import { ArrowLeft, ExclamationCircleFill } from "react-bootstrap-icons";
import ComponentForm from "~/components/componentsForm";
import { useComponentForm } from "~/hooks/use-component-form";
import { addImage, getComponent, updateComponent } from "~/services/components-service";
import type { ComponentFormValues } from "~/utils/component-form-utils";
import { requireAdminUser } from "~/services/route-guard";
import type { Route } from "./+types/components-update";

export async function clientLoader({ params }: Route.ClientLoaderArgs) {
  const [component] = await Promise.all([
    getComponent(Number(params.id!)),
    requireAdminUser(),
  ]);

  return { component };
}

export default function ComponentsUpdate({ loaderData }: Route.ComponentProps) {
  const { component } = loaderData;
  const navigate = useNavigate();

  const initialState: ComponentFormValues = {
    name: component.name ?? "",
    description: component.description ?? "",
    type: component.type ?? "",
    brand: component.brand ?? "",
    price: component.price ?? 0,
    stock: component.stock ?? 0,
  };

  const form = useComponentForm({
    initialState,
    defaultErrorMessage:"Hubo un error al actualizar el componente. Por favor, inténtalo de nuevo.",
    submitAction: async (values, image) => {
      const updatedComponent = await updateComponent(component.id, values.name, values.description, values.type, values.brand, values.price, values.stock);

      if (image && image.size > 0) {
        await addImage(updatedComponent.id, image);
      }

      await navigate(`/components/${updatedComponent.id}`);
    },
  });

  return (
    <main className="main">
      <div className="container">
        <div className="component-form">
          <Link to="/">
            <ArrowLeft />
            <span className="ms-2">Volver</span>
          </Link>

          <h3 id="update-component-title">
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
            component={component}
            onSubmit={form.handleSubmit}
            isPending={form.isPending}
            wasValidated={form.wasValidated}
            componentTypes={form.componentTypes}
            componentTypeLabels={form.componentTypeLabels}
            formRef={form.formRef}
            submitLabel="Actualizar componente"
          />
        </div>
      </div>
    </main>
  );
}
