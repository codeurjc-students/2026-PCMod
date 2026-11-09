import { useState } from "react";
import { Button, Image, Modal } from "react-bootstrap";
import { ArrowLeft, Trash } from "react-bootstrap-icons";
import type ReviewDTO from "~/dtos/ReviewDTO";
import { deleteReview } from "~/services/reviews-service";
import { useUserStore } from "~/stores/user-store";

interface ReviewCardProps {
  review: ReviewDTO;
  onDelete: (reviewId: number) => Promise<void>;
}

function convertRatingToStars(rating: number) {
  const ratingFormatted = rating.toFixed(0);
  const filled = "★".repeat(Number(ratingFormatted));
  const empty = "☆".repeat(5 - Number(ratingFormatted));
  return filled + empty;
}

export default function ReviewCard({ review, onDelete }: Readonly<ReviewCardProps>) {

  let { user } = useUserStore();

  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

  async function handleDeleteReview() {
    try {
      await deleteReview(review.componentId, review.id);
      setIsDeleteModalOpen(false);
      await onDelete(review.id);
    } catch (error) {
      console.error(error);
    }
  }

  return (
    <>
      <div className="review-card">
        <div className="user-info">
          <Image
            src={`/api/v1/users/${review.user.id}/image`}
            width="75"
            height="75"
            alt="userImage"
            className="rounded-circle"
          />
          <h3 id={`usernameReview-${review.user.id}`} className="usernameReview">{review.user.username}</h3>
          <h4 id={`dateReview-${review.id}`} className="dateReview">{review.date}</h4>
        </div>
        <div className="review-content">
          <h3 id={`review-title-${review.id}`}>{review.title}</h3>
          <h4 id={`review-message-${review.id}`}>{review.message}</h4>
          <h4 id={`review-valoration-${review.id}`}>Valoración del producto: {convertRatingToStars(review.rating)} {review.rating}/5</h4>
          {((user?.id === review.user.id) || (user?.roles.includes("ADMIN"))) && (
            <Button id={`delete-review-button-${review.id}`} name={`delete-review-button-${review.id}`} type="button" className="pcmod-danger-button" onClick={() => setIsDeleteModalOpen(true)}>
              <Trash aria-hidden="true" />
            </Button>
          )}
        </div>
      </div>

      <Modal show={isDeleteModalOpen} onHide={() => setIsDeleteModalOpen(false)}>
        <Modal.Header closeButton>
          <Modal.Title>¿Está seguro de que desea borrar esta reseña?</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p>
            Toda su información se perderá y no se podrá recuperar. Esta acción no se puede deshacer.
          </p>
        </Modal.Body>
        <Modal.Footer>
          <Button className="btn-secondary" onClick={() => setIsDeleteModalOpen(false)}>
            <ArrowLeft aria-hidden="true" />
            Cancelar
          </Button>
          <Button className="pcmod-danger-button" onClick={handleDeleteReview}>
            <Trash aria-hidden="true" />
            Eliminar reseña
          </Button>
        </Modal.Footer>
      </Modal>
    </>
  );
}