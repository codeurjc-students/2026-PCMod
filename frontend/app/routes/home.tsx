import { Outlet } from "react-router";
import Footer from "~/components/footer";
import Header from "~/components/header";

export default function Home() {
  return (
    <>
      <Header />
      <div>
        <Outlet />
      </div>
      <Footer />
    </>
  );
}