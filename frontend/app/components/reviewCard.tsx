import { Image } from "react-bootstrap";
import type ReviewDTO from "~/dtos/ReviewDTO";

function convertRatingToStars(rating: number) {
  const ratingFormatted = rating.toFixed(0);
  const filled = "★".repeat(Number(ratingFormatted));
  const empty = "☆".repeat(5 - Number(ratingFormatted));
  return filled + empty;
}

export default function ReviewCard({ review }: Readonly<{ review: ReviewDTO }>) {

  return (
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
      </div>
    </div>
  );
}