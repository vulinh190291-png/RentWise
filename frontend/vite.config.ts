import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: 'localhost',
    port: 5173,
    proxy: {
      '/map-local': { target: 'http://127.0.0.1:5175', changeOrigin: true },
      '/_AMapService': { target: 'http://127.0.0.1:5175', changeOrigin: true },
      '/api': {
        target: process.env.RENTWISE_API_PROXY || 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
