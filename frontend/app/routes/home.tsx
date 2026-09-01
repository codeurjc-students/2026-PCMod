import { Outlet, useNavigation } from "react-router";
import Footer from "~/components/footer";
import Header from "~/components/header";

export default function Home() {

  const navigation = useNavigation();
  const isLoading = navigation.state === "loading";

  return (
    <>
      {isLoading && (
        <div className="spinner-overlay">
          <div className="spinner" />
        </div>
      )}
      <Header />
      <div>
        <Outlet />
      </div>
      <Footer />
    </>
  );
}