import type { SubmitEvent, RefObject } from "react";
import { useState } from "react";
import { FormControl, FormLabel, FormSelect, Button } from "react-bootstrap";
import { ArrowLeft, Floppy } from "react-bootstrap-icons";
import { Link } from "react-router";
import type ComponentDTO from "~/dtos/ComponentDTO";

interface ComponentFormProps {
    component?: Partial<ComponentDTO>
    onSubmit: (event: SubmitEvent<HTMLFormElement>) => void;
    isPending: boolean;
    wasValidated: boolean;
    componentTypes: string[];
    componentTypeLabels: Record<string, string>;
    formRef: RefObject<HTMLFormElement | null>;
    submitLabel?: string;
}

export default function ComponentForm({
    component,
    onSubmit,
    isPending,
    wasValidated,
    componentTypes,
    componentTypeLabels,
    formRef,
    submitLabel,
}: Readonly<ComponentFormProps>) {

const [selectedType, setSelectedType] = useState(component?.type ?? "");
const cancelPath = component?.id !== undefined ? `/components/${component.id}` : "/components";

    return (
      <form
        ref={formRef}
        onSubmit={onSubmit}
        className={`custom-component-form needs-validation ${wasValidated ? "was-validated" : ""}`}
        noValidate
      >
        <div className="form">
          <div className="form-field">
            <FormLabel htmlFor="name" className="required-label" id="name-title">Nombre:</FormLabel>
            <FormControl type="text" disabled={isPending} className="form-control" name="name" id="name" defaultValue={component?.name ?? ""} required/>
            <div id="invalid-name" className="invalid-feedback">
              Por favor, ingrese un nombre válido.
            </div>
          </div>

          <div className="form-field">
            <FormLabel htmlFor="description" className="required-label" id="description-title">Descripción:</FormLabel>
            <FormControl as="textarea" disabled={isPending} className="form-control" name="description" id="description" defaultValue={component?.description ?? ""} required/>
            <div id="invalid-description" className="invalid-feedback">
              Por favor, ingrese una descripción válida.
            </div>
          </div>

          <div className="form-block">
            <div className="form-field">
              <FormLabel htmlFor="brand" className="required-label" id="brand-title">Marca:</FormLabel>
              <FormControl type="text" disabled={isPending} className="form-control" name="brand" id="brand" defaultValue={component?.brand ?? ""} required/>
              <div id="invalid-brand" className="invalid-feedback">
                Por favor, ingrese una marca válida.
              </div>
            </div>

            <div className="form-field">
              <FormLabel htmlFor="type" className="required-label" id="type-title">Tipo de componente:</FormLabel>
              <FormSelect disabled={isPending} className="form-control" name="type" id="type" required value={selectedType} onChange={(event) => setSelectedType(event.target.value)}>
                <option value="">--</option>
                {componentTypes.map(type => (
                  <option key={type} value={type}>
                    {componentTypeLabels[type] ?? type}
                  </option>
                ))}
              </FormSelect>
              <div id="invalid-type" className="invalid-feedback">
                Por favor, seleccione un tipo válido.
              </div>
            </div>
          </div>

          <div className="form-block">
            <div className="form-field">
              <FormLabel htmlFor="price" className="required-label" id="price-title">Precio:</FormLabel>
              <div className="price-input">
                <FormControl type="number" disabled={isPending} className="form-control" name="price" id="price" min="0.01" step="0.01" defaultValue={component?.price ?? ""} required/>
                <span className="price-suffix" aria-hidden="true">€</span>
                <div id="invalid-price" className="invalid-feedback">
                  Por favor, ingrese un precio válido.
                </div>
              </div>
            </div>

            <div className="form-field">
              <FormLabel htmlFor="stock" className="required-label" id="stock-title">Stock:</FormLabel>
              <FormControl type="number" disabled={isPending} className="form-control" name="stock" id="stock" min="0" defaultValue={component?.stock ?? ""} required/>
              <div id="invalid-stock" className="invalid-feedback">
                Por favor, ingrese un stock válido.
              </div>
            </div>
          </div>

          <div className="form-field">
            <FormLabel htmlFor="image" className="required-label" id="image-title">Imagen del componente:</FormLabel>
            <FormControl type="file" disabled={isPending} className="form-control" name="image" id="image" accept=".png,.jpg,.jpeg,.webp"/>
            <div id="invalid-image" className="invalid-feedback">
              Por favor, seleccione un archivo de imagen válido.
            </div>
          </div>
        </div>

        <Link to={cancelPath} id="cancel-component-button" className="pcmod-danger-button">
          <span className="me-2">Cancelar</span>
          <ArrowLeft />
        </Link>

        <Button type="submit" name="save-component-button" className="pcmod-button" disabled={isPending}>
          <span className="me-2">{submitLabel}</span>
          <Floppy />
        </Button>
      </form>
    );
}