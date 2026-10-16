import { useActionState, useRef, useState } from "react";
import type { SubmitEvent } from "react";
import { Button, FormControl, FormLabel } from "react-bootstrap";
import { ArrowLeft, BoxArrowInRight, ExclamationCircleFill, PersonPlus } from "react-bootstrap-icons";
import { Link, useNavigate } from "react-router";
import { register, addImage } from "~/services/users-service";
import { useUserStore } from "~/stores/user-store";
import { getUserFormValues, validateImage, handleFormSubmit } from "~/utils/user-form-utils";
import type { UserFormValues } from "~/utils/user-form-utils";

export default function Register() {

  const userStore = useUserStore();
  const navigate = useNavigate();
  const [wasValidated, setWasValidated] = useState(false);
  const formRef = useRef<HTMLFormElement>(null);
  const [errorMessage, setErrorMessage] = useState("");
  const [state, formAction, isPending] = useActionState(
    registerAction,
    { name: "", surname: "", username: "", address: "", email: "", password: "" }
  );

  async function registerAction(_prevState: UserFormValues, formData: FormData) {
    const { name, surname, username, address, email, password } = getUserFormValues(formData);
    const image = formData.get("image") as File | null;

    const imageError = validateImage(image);
    if (imageError) {
      setErrorMessage(imageError);
      return { name, surname, username, address, email, password };
    }

    try {
      const user = await register(name, surname, username, address, email, password);

      await userStore.loginUser(email, password);

      if (image && image.size > 0) {
        await addImage(user.id, image);
      }

      await navigate("/");
    } catch (error) {
      console.log(error);

      setErrorMessage(
        error instanceof Error
          ? error.message
          : "Hubo un error al completar el registro. Por favor, inténtalo de nuevo.",
      );
    }

    return { name, surname, username, address, email, password };
  }

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    handleFormSubmit(event, formAction, setWasValidated);
  }

  return (
    <main className="main">
      <div className="container">
        <div className="register-form">
          <Link to="/">
            <ArrowLeft />
            <span className="ms-2">Volver</span>
          </Link>
          <h3 id="register-title">Registro:</h3>
          {errorMessage && (
            <div className="error-message-text">
              <strong><ExclamationCircleFill /> Error:</strong>
              <p id="error-message" className="mb-0 mt-1">{errorMessage}</p>
            </div>
          )}
          <form ref={formRef} onSubmit={handleSubmit} className={`custom-register-form needs-validation ${wasValidated && "was-validated"}`} noValidate>
            <div className="form">
              <div className="form-field">
                <FormLabel htmlFor="name" name="name-title" className="required-label">Nombre:</FormLabel>
                <FormControl type="text" disabled={isPending} defaultValue={state.name} className="form-control" name="name" id="name" required />
                <div id="invalid-name" className="invalid-feedback">
                  Por favor, ingrese un nombre válido.
                </div>
              </div>
              <div className="form-field">
                <FormLabel htmlFor="surname" name="surname-title" className="required-label">Apellido:</FormLabel>
                <FormControl type="text" disabled={isPending} defaultValue={state.surname} className="form-control" name="surname" id="surname" required />
                <div id="invalid-surname" className="invalid-feedback">
                  Por favor, ingrese un apellido válido.
                </div>
              </div>
              <div className="form-field">
                <FormLabel htmlFor="username" name="username-title" className="required-label">Nombre de usuario:</FormLabel>
                <FormControl type="text" disabled={isPending} defaultValue={state.username} className="form-control" name="username" id="username" required />
                <div id="invalid-username" className="invalid-feedback">
                  Por favor, ingrese un nombre de usuario válido.
                </div>
              </div>
              <div className="form-field">
                <FormLabel htmlFor="address" name="address-title" className="required-label">Dirección:</FormLabel>
                <FormControl type="text" disabled={isPending} defaultValue={state.address} className="form-control" name="address" id="address" required />
                <div id="invalid-address" className="invalid-feedback">
                  Por favor, ingrese una dirección válida.
                </div>
              </div>
              <div className="form-field">
                <FormLabel htmlFor="email" name="email-title" className="required-label">Correo electrónico:</FormLabel>
                <FormControl type="email" disabled={isPending} defaultValue={state.email} className="form-control" name="email" id="email" placeholder="ejemplo@correo.com" required />
                <div id="invalid-email" className="invalid-feedback">
                  Por favor, ingrese un correo electrónico válido.
                </div>
              </div>
              <div className="form-field">
                <FormLabel htmlFor="password" name="password-title" className="required-label">Contraseña:</FormLabel>
                <FormControl type="password" disabled={isPending} defaultValue={state.password} className="form-control" name="password" id="password" placeholder="********" minLength={8} maxLength={20} required />
                <div id="invalid-password" className="invalid-feedback">
                  Por favor, ingrese una contraseña válida.
                </div>
              </div>
              <div className="form-field">
                <FormLabel htmlFor="image" name="image-title">Imagen de perfil:</FormLabel>
                <FormControl type="file" disabled={isPending} className="form-control" name="image" id="image" accept=".png,.jpg,.jpeg,.webp" />
                <div id="invalid-image" className="invalid-feedback">
                  Por favor, seleccione un archivo de imagen válido.
                </div>
              </div>
            </div>
            <Button type="submit" name="register-button" className="pcmod-button" disabled={isPending}>
              <span className="me-2">Registrarse</span>
              <PersonPlus />
            </Button>
            <p>¿Ya tiene cuenta en PCMod?</p>
            <Link to="/login" id="login-link" className="link-text">
              <BoxArrowInRight className="me-2" />Haga click aquí para iniciar sesión con su cuenta.
            </Link>
          </form>
        </div>
      </div >
    </main >
  );
}