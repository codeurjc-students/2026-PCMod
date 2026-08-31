import { Link } from "react-router";
import { Image as ImageIcon } from "react-bootstrap-icons";
import type ComponentDTO from "~/dtos/ComponentDTO";

interface ComponentCardProps {
  component: ComponentDTO;
}

export default function ComponentCard({ component }: ComponentCardProps) {
  return (
    <div>
      <Link to={`/components/${component.id}`} className="component-card-link">
        <div className="bg-secondary w-100 h-100 d-flex align-items-center justify-content-center text-white">
          <ImageIcon />
        </div>
        {/*
          component.image ? (
          <Image
            src={`/api/v1/images/${component.image.id}/media`}
            alt={component.name}
            className="img-fluid w-100 h-100"
            style={{ objectFit: "cover" }}
          />
          )
          */}

        <div className="component-information">
          <h3 id={`component-${component.id}`}>{component.name}</h3>
          <h4>{component.type} | {component.price} €</h4>
        </div>
      </Link>
    </div>
  );
}