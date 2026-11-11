import type { SubmitEvent, RefObject } from "react";
import { FormControl, FormLabel, Button } from "react-bootstrap";
import { Star } from "react-bootstrap-icons";

interface ReviewFormProps {
    onSubmit: (event: SubmitEvent<HTMLFormElement>) => void;
    isPending: boolean;
    wasValidated: boolean;
    formRef: RefObject<HTMLFormElement | null>;
    submitLabel?: string;
}

export default function ComponentForm({
    onSubmit,
    isPending,
    wasValidated,
    formRef,
    submitLabel,
}: Readonly<ReviewFormProps>) {

  return (
    <form
      ref={formRef}
      onSubmit={onSubmit}
      className={`custom-review-form needs-validation ${wasValidated ? "was-validated" : ""}`}
      noValidate
    >
      <div className="form">
        <h3 id="review-heading" >¡Añade una reseña a este producto!</h3>
        <div className="form-field">
          <FormLabel htmlFor="title" className="required-label" id="review-title-tag">Título:</FormLabel>
          <FormControl type="text" disabled={isPending} className="form-control" name="title" id="title" required/>
          <div id="invalid-title" className="invalid-feedback">
            Por favor, ingrese un título válido.
          </div>
        </div>

        <div className="form-field">
          <FormLabel htmlFor="message" className="required-label" id="message-title">Mensaje:</FormLabel>
          <FormControl as="textarea" disabled={isPending} className="form-control" name="message" id="message" required/>
          <div id="invalid-message" className="invalid-feedback">
            Por favor, ingrese un mensaje válido.
          </div>
        </div>

        <div className="form-field">
          <FormLabel htmlFor="rating" className="required-label" id="rating-title">Valoración:</FormLabel>
          <div className="rating-input">
            <FormControl type="number" disabled={isPending} className="form-control" name="rating" id="rating" min="1" max="5" step="1" required/>
            <div id="invalid-rating" className="invalid-feedback">
              Por favor, ingrese una valoración válida.
            </div>
          </div>
        </div>
      </div>

      <Button type="submit" id="save-review-button" name="save-review-button" className="pcmod-button" disabled={isPending}>
        <span className="me-2">{submitLabel}</span>
        <Star />
      </Button>
    </form>
  );
}