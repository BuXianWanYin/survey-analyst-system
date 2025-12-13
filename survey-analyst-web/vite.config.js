/**
 * Vite配置文件
 * 功能：配置Vite构建工具，包括开发服务器、代理、路径别名、构建选项
 */

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  
  const serverPort = parseInt(env.VITE_SERVER_PORT)
  const serverHost = env.VITE_SERVER_HOST
  const proxyTarget = env.VITE_SERVER_PROXY_TARGET
  const baseApi = env.VITE_APP_BASE_API
  
  return {
    plugins: [vue()],
    server: {
      host: serverHost,
      port: serverPort,
      proxy: {
        [baseApi]: {
          target: proxyTarget,
          changeOrigin: true,
          secure: false,
          configure: (proxy, options) => {
            proxy.on('proxyReq', (proxyReq, req, res) => {
              // 设置X-Forwarded-For头，传递客户端真实IP
              const clientIp = req.socket.remoteAddress || req.connection.remoteAddress
              if (clientIp) {
                // 处理IPv6映射的IPv4地址
                let ip = clientIp
                if (ip.startsWith('::ffff:')) {
                  ip = ip.replace('::ffff:', '')
                }
                // 如果已有X-Forwarded-For，追加；否则新建
                const existingForwardedFor = req.headers['x-forwarded-for']
                if (existingForwardedFor) {
                  proxyReq.setHeader('X-Forwarded-For', `${existingForwardedFor}, ${ip}`)
                } else {
                  proxyReq.setHeader('X-Forwarded-For', ip)
                }
                proxyReq.setHeader('X-Real-IP', ip)
              }
            })
          }
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

