import { useEffect, useState } from "react";
import type { Route } from "./+types/components";
import { getComponents } from "~/services/components-service";
import Col from "react-bootstrap/esm/Col";
import ComponentCard from "~/components/componentCard";
import { Button } from "react-bootstrap";
import { Plus, XOctagon } from "react-bootstrap-icons";

export async function clientLoader({ request }: Route.ClientLoaderArgs) {

  const url = new URL(request.url);
  const page = Number(url.searchParams.get("page") ?? 0);
  const result = await getComponents(page);

  return { items: result.items, hasNext: result.hasNext };
}

export default function Components({ loaderData }: Route.ComponentProps) {
  const [components, setComponents] = useState(loaderData.items);
  const [hasNext, setHasNext] = useState(loaderData.hasNext);
  const [currentPage, setCurrentPage] = useState(1);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [loadMoreError, setLoadMoreError] = useState(false);

  useEffect(() => {
    setComponents(loaderData.items);
    setHasNext(loaderData.hasNext);
    setCurrentPage(1);
    setIsLoadingMore(false);
    setLoadMoreError(false);
  }, [loaderData]);

  const handleLoadMore = async () => {
    if (isLoadingMore || !hasNext) {
      return;
    }

    setIsLoadingMore(true);
    setLoadMoreError(false);

    try {
      const result = await getComponents(currentPage);
      setComponents((previousComponents) => [...previousComponents, ...result.items]);
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
    <main className="main">
      <div className="container">
        <h1 id="components-title">Componentes:</h1>
        <div id="list-components" className="row-components">
          {components.length > 0 ? (
            <>
              {components.map((component) => (
                <Col lg={4} md={6} key={component.id} className="component-card-col">
                  <ComponentCard component={component} />
                </Col>
              ))}
            </>
          ) : (
            <Col className="col empty-components-state">
              <XOctagon />
              <p id="empty-message">
                No se han encontrado componentes de PC.
              </p>
            </Col>
          )}
        </div>
        <div className="load-more-container">
          {loadMoreError && (
            <p className="text-danger" role="alert">
              Hubo un problema al cargar más componentes. Por favor, pruebe de nuevo.
            </p>
          )}
          {components.length > 0 && hasNext && (
            <Button onClick={handleLoadMore} disabled={isLoadingMore} type="button" name="loadMore" className="pcmod-button">
              <Plus />
              {!isLoadingMore && (
                " Cargar más componentes"
              )}
            </Button>
          )}
        </div>
      </div>
    </main>
  );
}