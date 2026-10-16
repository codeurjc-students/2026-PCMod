import { startTransition, useActionState, useRef, useState } from "react";
import type { SubmitEvent } from "react";
import type { Route } from "./+types/profile";
import { addImage, deleteUser, getUser, updateUser } from "~/services/users-service";
import type UserDTO from "~/dtos/UserDTO";
import { CartX, Trash, Cart, ArrowLeft, PencilSquare, ExclamationCircleFill, Floppy, X } from 'react-bootstrap-icons';
import { Button, FormControl, FormLabel, Image, Modal } from "react-bootstrap";
import { useNavigate } from "react-router";
import { useUserStore } from "~/stores/user-store";

const API_URL = "/api/v1/users"

export async function clientLoader() {
  return await getUser()
}

export default function Profile({ loaderData }: Route.ComponentProps) {

  let { logoutUser } = useUserStore();

  const [profile, setProfile] = useState<UserDTO>(loaderData);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [wasValidated, setWasValidated] = useState(false);
  const formRef = useRef<HTMLFormElement>(null);
  const [errorMessage, setErrorMessage] = useState("");
  const [state, formAction, isPending] = useActionState(
    updateAction,
    { name: profile.name, surname: profile.surname, username: profile.username, address: profile.address, email: profile.email, password: "" }
  );
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

  async function updateAction(prevState: {}, formData: FormData) {
    const name = (formData.get("name") as string) ?? "";
    const surname = (formData.get("surname") as string) ?? "";
    const username = (formData.get("username") as string) ?? "";
    const address = (formData.get("address") as string) ?? "";
    const email = (formData.get("email") as string) ?? "";
    const password = (formData.get("password") as string) ?? "";
    const image = formData.get("image") as File | null;

    const form = formRef.current;
    setWasValidated(true)

    if (form?.checkValidity()) {
      if (image && image.size > 0) {
        const allowedImageTypes = ["image/jpeg", "image/png", "image/webp"];
        const maxImageSize = 10 * 1024 * 1024;

        if (image.size > maxImageSize || !allowedImageTypes.includes(image.type)) {
          setErrorMessage("Hubo un error al subir la imagen. Solo se permiten imágenes JPEG, PNG y WebP.");
          return { name, surname, username, address, email, password };
        }
      }

      try {
        const user = await updateUser(profile.id, name, surname, username, address, email, password);

        if (image && image.size > 0) {
          await addImage(user.id, image);
        }

        setProfile(user);
        setIsEditing(false);
        setWasValidated(false);
        setErrorMessage("");
      } catch (error) {
        console.log(error);
        setErrorMessage(error instanceof Error
          ? error.message
          : "Hubo un error al completar la actualización. Por favor, inténtalo de nuevo.");
      }
    }

    return { name, surname, username, address, email, password };
  }

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;

    if (!form.checkValidity()) {
      event.stopPropagation();
      setWasValidated(true);
    } else {
      setWasValidated(true);
      startTransition(() => {
        formAction(new FormData(form));
      });
    }
  }

  return (
    <>
      <main className="main">
        <div className="container">
          <div className="profile-data">
            {errorMessage && (
              <div className="error-message-text">
                <strong><ExclamationCircleFill /> Error:</strong>
                <p id="error-message" className="mb-0 mt-1">{errorMessage}</p>
              </div>
            )}
            <form ref={formRef} onSubmit={handleSubmit} className={`custom-register-form needs-validation ${wasValidated && "was-validated"}`} noValidate>
              <Image
                src={`${API_URL}/${profile.id}/image`}
                width="100"
                height="100"
                alt="UserImage"
                className="rounded-circle"
              />

              <div className="profile-fields">
                <div className="form-field">
                  {isEditing ? (
                    <>
                      <FormLabel htmlFor="name-form" className="required-label">Nombre:</FormLabel>
                      <FormControl type="text" disabled={isPending} defaultValue={profile.name} className="form-control" name="name" id="name-form" required />
                      <div className="invalid-name" id="invalid-name">Por favor, ingrese un nombre válido.</div>
                    </>
                  ) : (
                    <>
                      <dt>Nombre:</dt>
                      <dd id="name">{profile.name}</dd>
                    </>
                  )}
                </div>

                <div className="form-field">
                  {isEditing ? (
                    <>
                      <FormLabel htmlFor="surname-form" className="required-label">Apellido:</FormLabel>
                      <FormControl type="text" disabled={isPending} defaultValue={profile.surname} className="form-control" name="surname" id="surname-form" required />
                      <div className="invalid-surname" id="invalid-surname">Por favor, ingrese un apellido válido.</div>
                    </>
                  ) : (
                    <>
                      <dt>Apellidos:</dt>
                      <dd id="surname">{profile.surname}</dd>
                    </>
                  )}
                </div>

                <div className="form-field">
                  {isEditing ? (
                    <>
                      <FormLabel htmlFor="username-form" className="required-label">Nombre de usuario:</FormLabel>
                      <FormControl type="text" disabled={isPending} defaultValue={profile.username} className="form-control" name="username" id="username-form" required />
                      <div className="invalid-username" id="invalid-username">Por favor, ingrese un nombre de usuario válido.</div>
                    </>
                  ) : (
                    <>
                      <dt>Nombre de usuario:</dt>
                      <dd id="username">{profile.username}</dd>
                    </>
                  )}
                </div>

                <div className="form-field">
                  {isEditing ? (
                    <>
                      <FormLabel htmlFor="address-form" className="required-label">Dirección:</FormLabel>
                      <FormControl type="text" disabled={isPending} defaultValue={profile.address} className="form-control" name="address" id="address-form" required />
                      <div className="invalid-address" id="invalid-address">Por favor, ingrese una dirección válida.</div>
                    </>
                  ) : (
                    <>
                      <dt>Dirección:</dt>
                      <dd id="address">{profile.address}</dd>
                    </>
                  )}
                </div>

                <div className="form-field">
                  {isEditing ? (
                    <>
                      <FormLabel htmlFor="email-form" className="required-label">Correo electrónico:</FormLabel>
                      <FormControl type="email" disabled={isPending} defaultValue={profile.email} className="form-control" name="email" id="email-form" placeholder="ejemplo@correo.com" required />
                      <div className="invalid-email" id="invalid-email">Por favor, ingrese un correo electrónico válido.</div>
                    </>
                  ) : (
                    <>
                      <dt>Correo electrónico:</dt>
                      <dd id="email">{profile.email}</dd>
                    </>
                  )}
                </div>

                {isEditing && (
                  <div className="form-field">
                    <FormLabel htmlFor="password">Contraseña:</FormLabel>
                    <FormControl type="password" disabled={isPending} defaultValue={state.password} className="form-control" name="password" id="password" placeholder="********" minLength={8} maxLength={20} />
                    <div className="invalid-password" id="invalid-password">Por favor, ingrese una contraseña válida.</div>
                  </div>
                )}

                {isEditing && (
                  <div className="form-field">
                    <FormLabel htmlFor="image">Imagen de perfil:</FormLabel>
                    <FormControl type="file" disabled={isPending} className="form-control" name="image" id="image" accept=".png,.jpg,.jpeg,.webp" />
                    <div className="invalid-image" id="invalid-image">Por favor, seleccione un archivo de imagen válido.</div>
                  </div>
                )}
              </div>

              <div className="profile-actions">
                {!isEditing ? (
                  <>
                    <Button name="edit-button" type="button" className="pcmod-button"
                      onClick={(event) => {
                        event.preventDefault();
                        setIsEditing(true);
                      }}>
                      <PencilSquare aria-hidden="true" />
                      <span>Editar perfil</span>
                    </Button>
                    <Button name="delete-button" type="button" className="pcmod-danger-button" onClick={() => setIsDeleteModalOpen(true)}>
                      <Trash aria-hidden="true" />
                      <span>Borrar cuenta</span>
                    </Button>
                  </>
                ) : (
                  <>
                    <Button name="save-button" type="submit" className="pcmod-button" disabled={isPending}>
                      <Floppy />
                      <span>Guardar cambios</span>
                    </Button>
                    <Button name="cancel-button" type="button" className="pcmod-danger-button"
                      onClick={() => {
                        setIsEditing(false);
                        setWasValidated(false);
                        setErrorMessage("");
                      }}
                      disabled={isPending}
                    >
                      <X />
                      <span>Cancelar</span>
                    </Button>
                  </>
                )}
              </div>
            </form>
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
      </main>

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
          <Button className="pcmod-danger-button" onClick={handleDeleteAccount}>
            <Trash aria-hidden="true" />
            Eliminar cuenta
          </Button>
        </Modal.Footer>
      </Modal>
    </>
  );

}