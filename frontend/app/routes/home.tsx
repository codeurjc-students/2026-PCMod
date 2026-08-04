import { Outlet } from "react-router";

export default function Home() {
  return (
    <>
      <div>
        <Outlet />
      </div>
    </>
  );
}