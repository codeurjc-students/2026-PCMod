import { useEffect, useState } from "react";
import { Link } from "react-router";
import { Image } from "react-bootstrap";

export default function Header() {

  return <>
    <header id="header" className="header d-flex align-items-center sticky-top">
      <div className="header-content">

        <Link to="/" className="logo">
          <Image src="/Full_Logo.png" width="250" height="80" />
        </Link>

        <Link to="/components">Componentes</Link>

      </div>
    </header>
  </>;
}