import { useEffect, useState } from "react";
import type { Route } from "./+types/components";
import { getComponents } from "~/services/components-service";

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
        <h1>Componentes:</h1>
        <ul>
          {components.length > 0 && (
            components.map((component) => (
              <li key={component.id}>
                <h3 id={`name-${component.id}`}>{component.name}</h3>
                <h4>Marca: {component.brand} | Tipo: {component.type} | Precio: {component.price} € | Stock: {component.stock} ud.</h4>
                <p className="component-description">{component.description}</p>
                <hr></hr>
              </li>
            ))
          )}
        </ul>
        <div>
          {components.length > 0 && hasNext && (
            <button onClick={handleLoadMore} disabled={isLoadingMore} type="button" name="loadMore">
              {!isLoadingMore && (
                "Cargar más componentes"
              )}
            </button>
          )}
        </div>
      </div>
    </main>
  );
}