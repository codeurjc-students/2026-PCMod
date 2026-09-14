import { useEffect } from "react";
import { Link, useNavigate } from "react-router";
import { Dropdown, Image } from "react-bootstrap";
import { useUserStore } from "~/stores/user-store";
import { BoxArrowInRight, Person, PersonAdd, BoxArrowRight, ShieldLock } from "react-bootstrap-icons";

export default function Header() {
  let { user, loadLoggedUser, logoutUser } = useUserStore();
  const navigate = useNavigate();

  useEffect(() => {
    loadLoggedUser();
  }, [loadLoggedUser]);

  const handleLogout = () => {
    logoutUser();
    navigate("/");
  };

  return (
    <header id="header" className="header d-flex align-items-center sticky-top">
      <div className="header-content">
        <Link to="/" className="logo">
          <Image src="/Full_Logo.png" width="250" height="80" alt="Logo" />
        </Link>

        <Link to="/components">Componentes</Link>

        <div className="ms-auto">
          {user ? (
            <Dropdown align="end" className="user-dropdown">
              <Dropdown.Toggle
                as="button"
                className="avatar-toggle-btn"
                id="dropdown-user"
              >
                <Image
                  src="empty-profile-image.jpg"
                  width="45"
                  height="45"
                  alt="User"
                  className="rounded-circle"
                />
              </Dropdown.Toggle>

              <Dropdown.Menu>
                <Dropdown.Item as={Link} id="profile-option" className="menu-option" to={`/users/${user.id}`}>
                  <Person /> Mi perfil
                </Dropdown.Item>

                {user.roles?.includes("ADMIN") && (
                  <>
                    <Dropdown.Divider />
                    <Dropdown.Item as={Link} id="administration-option" className="menu-option" to="/admin">
                      <ShieldLock /> Administración
                    </Dropdown.Item>
                  </>
                )}

                <Dropdown.Divider />
                <Dropdown.Item
                  id="logout-option"
                  className="menu-option"
                  style={{ textAlign: 'left', border: 'none' }}
                  onClick={handleLogout}
                >
                  <BoxArrowRight />
                  Cerrar sesión
                </Dropdown.Item>
              </Dropdown.Menu>
            </Dropdown>
          ) : (
            <>
              <Link className="header-btn" to="/login">
                <BoxArrowInRight />
                Iniciar Sesión
              </Link>
              <Link className="header-btn" to="/register">
                <PersonAdd />
                Registrarse
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}