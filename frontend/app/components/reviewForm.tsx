import type { SubmitEvent, RefObject } from "react";
import { FormControl, FormLabel, Button } from "react-bootstrap";
import { Star, X } from "react-bootstrap-icons";
import type ReviewDTO from "~/dtos/ReviewDTO";

interface ReviewFormProps {
    review?: Partial<ReviewDTO>
    onSubmit: (event: SubmitEvent<HTMLFormElement>) => void;
    isPending: boolean;
    wasValidated: boolean;
    formRef: RefObject<HTMLFormElement | null>;
    submitLabel?: string;
    onCancel?: () => void;
}

export default function ReviewForm({
    review,
    onSubmit,
    isPending,
    wasValidated,
    formRef,
    submitLabel,
    onCancel
}: Readonly<ReviewFormProps>) {

  return (
    <form
      ref={formRef}
      onSubmit={onSubmit}
      className={`custom-review-form needs-validation ${wasValidated ? "was-validated" : ""}`}
      id={review ? 'edit-review-form' : 'create-review-form'}
      noValidate
    >
      <div className="form">
        <h3 id="review-heading" >¡Añade una reseña a este producto!</h3>
        <div className="form-field">
          <FormLabel htmlFor="title" className="required-label" id="review-title-tag">Título:</FormLabel>
          <FormControl type="text" disabled={isPending} className="form-control" name="title" id="title" aria-labelledby="review-title-tag" defaultValue={review?.title ?? ""} required/>
          <div id="invalid-title" className="invalid-feedback">
            Por favor, ingrese un título válido.
          </div>
        </div>

        <div className="form-field">
          <FormLabel htmlFor="message" className="required-label" id="message-title">Mensaje:</FormLabel>
          <FormControl as="textarea" disabled={isPending} className="form-control" name="message" id="message" aria-labelledby="message-title" defaultValue={review?.message ?? ""} required/>
          <div id="invalid-message" className="invalid-feedback">
            Por favor, ingrese un mensaje válido.
          </div>
        </div>

        <div className="form-field">
          <FormLabel htmlFor="rating" className="required-label" id="rating-title">Valoración:</FormLabel>
          <div className="rating-input">
            <FormControl type="number" disabled={isPending} className="form-control" name="rating" id="rating" aria-labelledby="rating-title" min="1" max="5" step="1" defaultValue={review?.rating ?? ""} required/>
            <div id="invalid-rating" className="invalid-feedback">
              Por favor, ingrese una valoración válida.
            </div>
          </div>
        </div>
      </div>

      <div className="review-form-actions">
        {onCancel && (
          <Button id="cancel-review-button" name="cancel-review-button" type="button" className="pcmod-danger-button" onClick={onCancel}>
            <X aria-hidden="true" />
            <span className="me-2">Cancelar</span>
          </Button>
        )}
        <Button type="submit" id="save-review-button" name="save-review-button" className="pcmod-review-update-button" disabled={isPending}>
          <Star />
          <span className="me-2">{submitLabel}</span>
        </Button>
      </div>
    </form>
  );
}