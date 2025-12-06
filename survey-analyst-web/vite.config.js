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
      port: serverPort,
      proxy: {
        [baseApi]: {
          target: proxyTarget,
          changeOrigin: true,
          secure: false
          // 不重写路径，保持/api前缀，因为后端接口路径就是/api/auth/login
          // rewrite: (path) => path.replace(new RegExp(`^${baseApi}`), '')
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

