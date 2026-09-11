import { useActionState, useRef, useState, startTransition } from "react";
import type { SubmitEvent } from "react";
import { Button, FormControl, FormLabel } from "react-bootstrap";
import { ArrowLeft, BoxArrowInRight, ExclamationCircleFill, PersonPlus } from "react-bootstrap-icons";
import { Link, useNavigate } from "react-router";
import { useUserStore } from "~/stores/user-store";

export default function Login() {
  const userStore = useUserStore()
  const navigate = useNavigate()
  const [wasValidated, setWasValidated] = useState(false)
  const formRef = useRef<HTMLFormElement>(null);
  const [errorMessage, setErrorMessage] = useState("")
  const [state, formAction, isPending] = useActionState(
    loginAction,
    { email: "", password: "" }
  )

  async function loginAction(prevState: {}, formData: FormData) {

    const email = (formData.get("email") as string) ?? "";
    const password = (formData.get("password") as string) ?? "";

    const form = formRef.current;
    setWasValidated(true)

    if (form?.checkValidity()) {

      try {

        await userStore.loginUser(email, password)
        const error = useUserStore.getState().loginError;

        if (error) {
          setErrorMessage(error)
        } else {
          navigate(`/`);
        }

      } catch (error) {

        console.log(error);
        setErrorMessage("Hubo un error al iniciar sesión. Por favor, inténtalo de nuevo.");

      }

    }

    return { email, password }
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
    <main className="main">
      <div className="container">
        <div className="login-form">
          <Link to="/">
            <ArrowLeft />
            <span className="ms-2">Volver</span>
          </Link>
          <h3 id="login-title">Iniciar sesión:</h3>
          {errorMessage && (
            <div className="error-message-text">
              <strong><ExclamationCircleFill /> Error:</strong>
              <p id="error-message" className="mb-0 mt-1">{errorMessage}</p>
            </div>
          )}
          <form ref={formRef} onSubmit={handleSubmit} className={`custom-login-form needs-validation ${wasValidated && "was-validated"}`} noValidate>
            <div className="form">
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
                  Contraseña incorrecta. Inténtelo de nuevo.
                </div>
              </div>
            </div>
            <Button type="submit" name="login-button" className="pcmod-button" disabled={isPending}>
              <span className="me-2">Iniciar sesión</span>
              <BoxArrowInRight />
            </Button>
            <p>¿No tiene cuenta en PCMod?</p>
            <Link to="/register" id="register-link" className="link-text">
              <PersonPlus className="me-2" />Haga click aquí para crear una.
            </Link>
          </form>
        </div>
      </div >
    </main >
  );
}