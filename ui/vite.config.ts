import { defineConfig, loadEnv } from 'vite'
import { svelte } from '@sveltejs/vite-plugin-svelte'
import 'dotenv/config'

// https://vitejs.dev/config/
export default defineConfig(({mode}) => {
  if (mode !== "production" && !process.env.VITE_KUBERNETES_API_URL) {
    console.log('⚠️  WARNING ⚠️ :environment variable VITE_KUBERNETES_API_URL is not defined. API may not be working!')
  }
  return {
    plugins: [svelte()],
    server: {
      proxy: {
        '/apis': {
          target: process.env.VITE_KUBERNETES_API_URL,
          changeOrigin: true,
          secure: false,
        },
        "/settings": {
          // this is for running "go run . webui"
          target: "http://localhost:8080",
          changeOrigin: true,
          secure: false,
        }
      }
    }
  }
})
