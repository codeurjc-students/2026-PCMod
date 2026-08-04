import { reactRouter } from "@react-router/dev/vite";
import { defineConfig } from "vite";
import tsconfigPaths from "vite-tsconfig-paths";

export default defineConfig(({ mode }) => ({
  base: "/",
  plugins: [mode !== 'test' && reactRouter(), tsconfigPaths()],
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
  test: {
    coverage: {
      provider: "v8",
      reporter: [
        'text',
        'lcov'
      ]
    },
    environment: "jsdom",
    environmentOptions: {
      jsdom: {
        url: "https://localhost:8443",
      },
    },
    env: {
      NODE_TLS_REJECT_UNAUTHORIZED: '0',
    },
    globals: true,
  }
}));
