import { Button, Col, Image, Modal } from "react-bootstrap";
import { ArrowLeft, CartPlus, Image as ImageIcon, PencilSquare, Plus, Trash, XOctagon } from "react-bootstrap-icons";
import type { Route } from "./+types/detail-component";
import { deleteComponent, getComponent } from "~/services/components-service";
import { useUserStore } from "~/stores/user-store";
import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import ReviewCard from "~/components/reviewCard";
import type ReviewDTO from "~/dtos/ReviewDTO";
import { getReviews } from "~/services/reviews-service";

const API_URL = "/api/v1/components";

export async function clientLoader({params}: Route.ClientLoaderArgs) {
  const component = await getComponent(Number(params.id));
  const reviews = await getReviews(component.id, 0);
  return { component, reviews };
}

function convertRatingToStars(rating: number) {
  const ratingFormatted = rating.toFixed(0);
  const filled = "★".repeat(Number(ratingFormatted));
  const empty = "☆".repeat(5 - Number(ratingFormatted));
  return filled + empty;
}

export default function ComponentDetail({ loaderData }: Route.ComponentProps) {

  const component = loaderData.component;
  const price = component.price.toFixed(2);
  let { user } = useUserStore();
  const navigate = useNavigate();

  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [reviews, setReviews] = useState<ReviewDTO[]>(loaderData.reviews.items);
  const [hasNext, setHasNext] = useState(loaderData.reviews.hasNext);
  const [currentPage, setCurrentPage] = useState(1);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [loadMoreError, setLoadMoreError] = useState(false);
  
  useEffect(() => {
    setReviews(loaderData.reviews.items);
    setHasNext(loaderData.reviews.hasNext);
    setCurrentPage(1);
    setIsLoadingMore(false);
    setLoadMoreError(false);
  }, [loaderData]);

  async function handleDeleteComponent() {
    try {
      await deleteComponent(component.id);
      await navigate("/components");
    } catch (error) {
      console.error(error);
    }
  }

  const handleLoadMore = async () => {
    if (isLoadingMore || !hasNext) {
      return;
    }

    setIsLoadingMore(true);
    setLoadMoreError(false);

    try {
      const result = await getReviews(component.id, currentPage);
      setReviews((previousReviews) => [...previousReviews, ...result.items]);
      setHasNext(result.hasNext);

      if (result.hasNext) {
        setCurrentPage((previousPage) => previousPage + 1);
      }
    } catch {
      setLoadMoreError(true);
    } finally {
      setIsLoadingMore(false);
    }
  };

  return (
    <>
      <main className="main">
        <div className="container">
          <section className="component-data">
            <Col className="component-image" id="component-image" xs={12} md={4}>
            {component.image ? (
              <Image
                src={`${API_URL}/${component.id}/image`}
                alt={component.name}
              />
            ) : (
              <ImageIcon />
            )}
            </Col>
            <Col className="component-info" xs={12} md={8}>
              {component.stock <= 5 && component.stock > 0 && (
                <h4 className="component-stock" id="component-stock">¡Últimas unidades!</h4>
              )}
              {component.stock === 0 && (
                <h4 className="component-stock" id="component-stock">¡No hay stock!</h4>
              )}
              <h1 id="component-name">{component.name}</h1>
              <p className="component-description" id="component-description">
                {component.description}
              </p>
              <h4 id="component-brand-type">
                {component.brand} | {component.type}
              </h4>
              <h4 id="component-price">{price} €</h4>
              <div className="component-actions">
                <Button className="pcmod-button" id="add-to-cart-button" disabled={component.stock === 0}>
                  <CartPlus />
                  <span>
                    {component.stock === 0 ? 'No hay stock' : 'Añadir al carrito'}
                  </span>
                </Button>
                {user?.roles?.includes("ADMIN") && (
                  <>
                    <Link to={`/components/${component.id}/update`} className="btn-secondary" id="edit-component-button">
                      <PencilSquare />
                    </Link>
                    <Button className="pcmod-danger-button" id="delete-component-button" onClick={() => setIsDeleteModalOpen(true)}>
                      <Trash />
                    </Button>
                  </>
                )}
              </div>
            </Col>
          </section>
          <section className="component-reviews">
            <h2 id="component-reviews-title">Reseñas del producto:</h2>
            <h3 id="component-average-rating">Puntuación media: <span className="rating-stars">{convertRatingToStars(component.averageRating)}</span> {component.averageRating}/5 ({component.reviews.length} reseña(s))</h3>
            <div className="reviews-list">
              {reviews.length > 0 ? (
                <>
                  {reviews.map((review) => (
                    <ReviewCard key={review.id} review={review} />
                  ))}
                  <div className="load-more-container">
                    {loadMoreError && (
                      <p className="text-danger" role="alert">
                        Hubo un problema al cargar más reseñas. Por favor, pruebe de nuevo.
                      </p>
                    )}
                    {hasNext && (
                      <Button onClick={handleLoadMore} disabled={isLoadingMore} type="button" id="loadMore" name="loadMore" className="pcmod-button">
                        <Plus />
                        {!isLoadingMore && " Cargar más reseñas"}
                      </Button>
                    )}
                  </div>
                </>
              ) : (
                <>
                  <XOctagon />
                  <h4 id="component-no-reviews">
                    No hay reseñas de este producto todavía.
                  </h4>
                </>
              )}
            </div>
          </section>
        </div>
      </main>

      <Modal show={isDeleteModalOpen} onHide={() => setIsDeleteModalOpen(false)}>
        <Modal.Header closeButton>
          <Modal.Title>¿Está seguro de que desea borrar este componente?</Modal.Title>
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
          <Button className="pcmod-danger-button" onClick={handleDeleteComponent}>
            <Trash aria-hidden="true" />
            Eliminar componente
          </Button>
        </Modal.Footer>
      </Modal>
    </>
  );

}