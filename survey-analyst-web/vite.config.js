import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  
  const serverPort = parseInt(env.VITE_SERVER_PORT)
  const proxyTarget = env.VITE_SERVER_PROXY_TARGET
  const baseApi = env.VITE_APP_BASE_API
  
  return {
    plugins: [vue()],
    server: {
      host: '0.0.0.0', // 允许外部访问，包括手机
      port: serverPort,
      proxy: {
        [baseApi]: {
          target: proxyTarget,
          changeOrigin: true,
          secure: false
        }
      }
    },
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    build: {
      outDir: 'dist',
      assetsDir: 'assets',
      sourcemap: false,
      chunkSizeWarningLimit: 1500
    }
  }
})

