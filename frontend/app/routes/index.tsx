import { Button, Col, Image } from "react-bootstrap";
import { useNavigate } from "react-router";
import { XOctagon } from "react-bootstrap-icons";
import type { Route } from "./+types";
import ComponentCard from "~/components/componentCard";
import { useState } from "react";
import type ComponentDTO from "~/dtos/ComponentDTO";
import { getRecentComponents } from "~/services/components-service";

export async function clientLoader() {
  const recentComponents = await getRecentComponents();
  return {
    recentComponents,
  };
}

export default function Index({ loaderData }: Route.ComponentProps) {
  const navigate = useNavigate();

  const [recentComponents] = useState<ComponentDTO[]>(loaderData.recentComponents);

  return (
    <main className="main">
      <div className="container">
        <section className="row">
          <Col lg={6} md={12} className="col">
            <Image id="logo" src="/Full_Logo.png" fluid alt="PCMod Logo" />
          </Col>

          <Col lg={6} md={12} className="col">
            <h1 id="welcome">Bienvenido a PCMod</h1>
            <p>
              PCMod es una plataforma de compra online de componentes de ordenador. En nuestra tienda podrá encontrar una amplia selección de productos para montar su nuevo PC o actualizar el que ya tiene.
            </p>
            <p>
              Buscamos ofrecer la mejor experiencia para nuestros clientes, y, por ello, ofrecemos un servicio de compra sencillo y eficiente. Nuestros clientes cuentan con un servicio de confirmación de operaciones por correo electrónico, un sistema de pago seguro y un asistente potenciado por IA.
            </p>
            <p>
              Si tiene cualquier duda, no dude en ponerse en contacto con nosotros:{' '}<a href="mailto:...."><span>....</span></a>
            </p>
          </Col>
        </section>

        <section className="recentComponents-section">

          <h2>Descubra nuestras últimas novedades:</h2>

          <div className="row-recentComponents">
            {recentComponents.length > 0 ? (
              <>
                {recentComponents.map((component) => (
                  <Col lg={4} md={6} key={component.id} className="component-card-col">
                    <ComponentCard component={component} />
                  </Col>
                ))}

                <Button className="pcmod-button" name="componentsButton" onClick={() => navigate("/components")}>
                  Ver todos los componentes
                </Button>
              </>
            ) : (
              <Col className="col">
                <XOctagon />
                <p>
                  No se han encontrado componentes de PC.
                </p>
              </Col>
            )}
          </div>

        </section>
      </div >
    </main >
  );
}