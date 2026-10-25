import { Button, Col, Image } from "react-bootstrap";
import { CartPlus, Image as ImageIcon, PencilSquare, Trash, XOctagon } from "react-bootstrap-icons";
import type { Route } from "./+types/detail-component";
import type ComponentDTO from "~/dtos/ComponentDTO";
import { getComponent } from "~/services/components-service";
import { useUserStore } from "~/stores/user-store";

const API_URL = "/api/v1/components";

export async function clientLoader({params}: Route.ClientLoaderArgs): Promise<ComponentDTO> {
  return await getComponent(Number(params.id));
}

export default function ComponentDetail({ loaderData }: Route.ComponentProps) {

  const component = loaderData;
  const price = component.price.toFixed(2);
  let { user } = useUserStore();

  return (
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
                    <Button className="pcmod-danger-button" id="delete-component-button">
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
  );

}