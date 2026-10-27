import { Button, Col, Image, Modal } from "react-bootstrap";
import { ArrowLeft, CartPlus, Image as ImageIcon, PencilSquare, Trash, XOctagon } from "react-bootstrap-icons";
import type { Route } from "./+types/detail-component";
import type ComponentDTO from "~/dtos/ComponentDTO";
import { deleteComponent, getComponent } from "~/services/components-service";
import { useUserStore } from "~/stores/user-store";
import { useState } from "react";
import { useNavigate } from "react-router";

const API_URL = "/api/v1/components";

export async function clientLoader({params}: Route.ClientLoaderArgs): Promise<ComponentDTO> {
  return await getComponent(Number(params.id));
}

export default function ComponentDetail({ loaderData }: Route.ComponentProps) {

  const component = loaderData;
  const price = component.price.toFixed(2);
  let { user } = useUserStore();
  const navigate = useNavigate();

  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

  async function handleDeleteComponent() {
    try {
      await deleteComponent(component.id);
      await navigate("/components");
    } catch (error) {
      console.error(error);
    }
  }

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
              {component.stock <= 5 && (
                <h4 className="component-stock" id="component-stock">¡Últimas unidades!</h4>
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
                <Button className="pcmod-button" id="add-to-cart-button">
                  <CartPlus />
                  <span> Añadir al carrito</span>
                </Button>
                {user?.roles?.includes("ADMIN") && (
                  <>
                    <Button className="btn-secondary" id="edit-component-button">
                      <PencilSquare />
                    </Button>
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
            <h3 id="component-average-rating">Puntuación media: N/A</h3>
            <div className="reviews-list">
              <XOctagon />
              <h4 id="component-no-reviews"> No hay reseñas de este producto todavía.</h4>
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