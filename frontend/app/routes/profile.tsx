import { useState } from "react";
import type { Route } from "./+types/profile";
import { deleteUser, getUser } from "~/services/users-service";
import type UserDTO from "~/dtos/UserDTO";
import { CartX, Trash, Cart, ArrowLeft } from 'react-bootstrap-icons';
import { Button, Image, Modal } from "react-bootstrap";
import { useNavigate } from "react-router";
import { useUserStore } from "~/stores/user-store";

const API_URL = "/api/v1/users"

export async function clientLoader() {
  return await getUser()
}

export default function Profile({ loaderData }: Route.ClientLoaderArgs) {

  let { logoutUser } = useUserStore();

  const [profile] = useState<UserDTO>(loaderData);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const navigate = useNavigate();

  async function handleDeleteAccount() {

    try {

      await deleteUser(profile.id);
      await logoutUser();
      await navigate("/");

    } catch (err) {

      console.error(err);

    }
  }

  return (
    <>
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

            <div className="profile-actions">
              <Button name="delete-button" className="pcmod-danger-button" onClick={() => setIsDeleteModalOpen(true)}>
                <Trash aria-hidden="true" />
                <span>Borrar cuenta</span>
              </Button>
            </div>
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

      <Modal show={isDeleteModalOpen} onHide={() => setIsDeleteModalOpen(false)}>
        <Modal.Header closeButton>
          <Modal.Title>¿Está seguro de que desea borrar su cuenta?</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p>
            Todos sus datos se perderán y no podrá recuperarlos. Esta acción no se puede deshacer.
          </p>
        </Modal.Body>
        <Modal.Footer>
          <Button className="btn-secondary" onClick={() => setIsDeleteModalOpen(false)}>
            <ArrowLeft aria-hidden="true" />
            Cancelar
          </Button>
          <Button className="pcmod-btn-danger" onClick={handleDeleteAccount}>
            <Trash aria-hidden="true" />
            Eliminar cuenta
          </Button>
        </Modal.Footer>
      </Modal>
    </>
  );

}