import { type RouteConfig, layout, route } from "@react-router/dev/routes";

export default [
  layout("routes/home.tsx", [
    route("/", "routes/index.tsx"),
  ]),
] satisfies RouteConfig;
