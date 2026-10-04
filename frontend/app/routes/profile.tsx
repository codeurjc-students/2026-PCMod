import { useState } from "react";
import type { Route } from "./+types/profile";
import { getUser } from "~/services/users-service";
import type UserDTO from "~/dtos/UserDTO";
import { CartX, PencilSquare, Trash, Cart } from 'react-bootstrap-icons';
import { Button, Image } from "react-bootstrap";

const API_URL = "/api/v1/users"

export async function clientLoader() {
  return await getUser()
}

export default function Profile({ loaderData }: Route.ClientLoaderArgs) {

  const [profile] = useState<UserDTO>(loaderData);

  return (
    <main className="main">
      <div className="container">
        <div className="profile-data">
          <Image
            src={`${API_URL}/${profile.id}/image`}
            width="100"
            height="100"
            alt="UserImage"
            className="rounded-circle"
          />
          <dl className="profile-fields">
            <div>
              <dt>Nombre:</dt>
              <dd id="name">{profile.name}</dd>
            </div>
            <div>
              <dt>Apellidos:</dt>
              <dd id="surname">{profile.surname}</dd>
            </div>
            <div>
              <dt>Nombre de usuario:</dt>
              <dd id="username">{profile.username}</dd>
            </div>
            <div>
              <dt>Dirección:</dt>
              <dd id="address">{profile.address}</dd>
            </div>
            <div>
              <dt>Correo electrónico:</dt>
              <dd id="email">{profile.email}</dd>
            </div>
          </dl>
        </div>
        <div className="purchases-data">
          <div className="purchases-header">
            <div>
              <Cart aria-hidden="true" />
              <h2 id="purchases">Mis compras:</h2>
            </div>
          </div>
          <div className="empty-purchases">
            <CartX aria-hidden="true" />
            <p>No se han realizado compras todavía.</p>
          </div>
        </div>
      </div>
    </main >
  );

}