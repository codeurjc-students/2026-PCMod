import { reactRouter } from "@react-router/dev/vite";
import { defineConfig } from "vite";
import tsconfigPaths from "vite-tsconfig-paths";

export default defineConfig(({ }) => ({
  base: "/",
  plugins: [reactRouter(), tsconfigPaths()],
  server: {
    proxy: {
      "/api": {
        target: "https://localhost:8443/api",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ""),
        secure: false,
      },
    },
  },
}));