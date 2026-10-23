import { Link } from "react-router";
import { Image } from "react-bootstrap";
import { Image as ImageIcon } from "react-bootstrap-icons";
import type ComponentDTO from "~/dtos/ComponentDTO";

const API_URL = "/api/v1";

export default function ComponentCard({ component }: Readonly<{ component: ComponentDTO }>) {
  return (
    <div>
      <Link to={`/components/${component.id}`} className="component-card-link">
        <>
          <div className="component-card-image bg-secondary w-100 h-100 d-flex align-items-center justify-content-center text-white">
            {component.image ? (
              <Image
                src={`${API_URL}/components/${component.id}/image`}
                alt={component.name}
              />
            ) : (
              <ImageIcon />
            )
            }
          </div>

          <div className="component-information">
            <h3 id={`component-${component.id}`}>{component.name}</h3>
            <h4>{component.type} | {component.price} €</h4>
          </div>
        </>
      </Link>
    </div>
  );
}