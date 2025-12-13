# Vite 代理配置说明文档

## 一、配置概述

本项目使用 Vite 作为前端开发服务器，通过代理配置将前端 API 请求转发到后端服务器。主要涉及以下环境变量配置：

- `VITE_SERVER_PROXY_TARGET` - 代理目标地址（后端服务地址）
- `VITE_SERVER_HOST` - 开发服务器主机地址
- `VITE_SERVER_PORT` - 开发服务器端口
- `VITE_APP_BASE_API` - API 基础路径

---

## 二、VITE_SERVER_PROXY_TARGET 配置说明

### 2.1 配置作用

`VITE_SERVER_PROXY_TARGET` 用于指定后端服务器的地址，所有以 `VITE_APP_BASE_API` 开头的请求都会被代理转发到这个地址。

### 2.2 配置格式

```bash
# 格式：http://IP地址:端口
VITE_SERVER_PROXY_TARGET=http://192.168.1.100:8080
```

### 2.3 配置示例

#### 示例 1：本地开发（后端在本地）
```bash
VITE_SERVER_PROXY_TARGET=http://localhost:8080
# 或
VITE_SERVER_PROXY_TARGET=http://127.0.0.1:8080
```

#### 示例 2：局域网内其他机器
```bash
# 假设后端服务器在局域网内的 IP 地址是 192.168.1.100，端口是 8080
VITE_SERVER_PROXY_TARGET=http://192.168.1.100:8080
```


### 2.4 配置位置

在项目根目录下创建 `.env` 或 `.env.development` 文件（根据开发模式选择）：

```bash
# .env.development（开发环境）
VITE_SERVER_PROXY_TARGET=http://192.168.1.100:8080
VITE_SERVER_HOST=0.0.0.0
VITE_SERVER_PORT=3001
VITE_APP_BASE_API=/api
```

---

## 三、手机访问配置说明

### 3.1 为什么需要特殊配置？

默认情况下，Vite 开发服务器只绑定到 `localhost` 或 `127.0.0.1`，这意味着：
- 本机可以通过 `http://localhost:3001` 访问
- 同一局域网内的其他设备（如手机）无法访问

要让手机能够访问开发服务器，需要将 `VITE_SERVER_HOST` 设置为 `0.0.0.0`。

### 3.2 配置步骤

#### 步骤 1：配置开发服务器主机地址

在 `.env.development` 文件中设置：

```bash
# 允许所有网络接口访问（包括局域网）
VITE_SERVER_HOST=0.0.0.0
```

#### 步骤 2：确保防火墙允许访问

**Windows 系统：**
1. 打开"Windows Defender 防火墙"
2. 点击入站规则
3. 把前端端口添加到入栈规则中


#### 步骤 3：获取本机 IP 地址

**Windows 系统：**
```powershell
# 在 PowerShell 中执行
ipconfig
# 查找"IPv4 地址"，例如：192.168.1.50
```

#### 步骤 4：在手机上访问

1. 确保手机和电脑连接在**同一个 WiFi 网络**下
2. 在手机浏览器中输入：`http://你的IP地址:端口号`
   - 例如：`http://192.168.1.50:3001`

### 3.3 完整配置示例（支持手机访问）

```bash
# .env.development
# 开发服务器配置
VITE_SERVER_HOST=0.0.0.0          # 允许所有网络接口访问
VITE_SERVER_PORT=3001              # 开发服务器端口

# 代理配置
VITE_SERVER_PROXY_TARGET=http://192.168.1.100:8080  # 后端服务器实际ip地址
VITE_APP_BASE_API=/api             # API 基础路径
```


#### 示例 1：允许局域网访问（推荐用于手机调试）
```bash
VITE_SERVER_HOST=0.0.0.0
```
**访问地址：**
- 本机：`http://localhost:3001` 或 `http://127.0.0.1:3001`
- 局域网：`http://192.168.1.50:3001`（使用实际 IP）

#### 示例 3：绑定到特定网卡
```bash
# 假设有多个网卡，想绑定到特定 IP
VITE_SERVER_HOST=192.168.1.50
```

---

## 五、配置原理说明

### 5.1 为什么需要代理？

在前后端分离的开发模式下：
- **前端**：运行在 Vite 开发服务器（如 `http://localhost:3001`）
- **后端**：运行在独立的服务器（如 `http://192.168.1.100:8080`）

如果前端直接请求后端地址，会遇到**跨域问题**（CORS），因为：
- 协议、域名、端口不同
- 浏览器同源策略限制

**解决方案：** 使用 Vite 的代理功能，将前端的 API 请求转发到后端服务器。

### 5.2 代理工作流程

```
手机/浏览器
    ↓
   请求: http://192.168.1.50:3001/api/user/login
    ↓
Vite 开发服务器（代理）
    ↓
   转发: http://192.168.1.100:8080/api/user/login
    ↓
后端服务器
    ↓
   响应返回
```

### 5.3 配置项说明

在 `vite.config.js` 中的代理配置：

```javascript
proxy: {
  [baseApi]: {  // 例如：'/api'
    target: proxyTarget,        // 目标服务器地址
    changeOrigin: true,          // 改变请求头中的 origin
    secure: false,               // 如果是 https，是否验证证书
  }
}
```

**各配置项作用：**

1. **`target`**：代理的目标服务器地址
2. **`changeOrigin: true`**：
   - 改变请求头中的 `Origin` 字段
   - 避免后端服务器因为 Origin 不匹配而拒绝请求
   - **必须设置为 `true`**，否则可能遇到 CORS 问题
3. **`secure: false`**：
   - 如果目标服务器使用 HTTPS 但证书无效，设置为 `false` 可以跳过证书验证
   - 仅用于开发环境

---

## 六、常见问题与解决方案

### 6.1 问题：手机无法访问开发服务器

**原因：**
- `VITE_SERVER_HOST` 设置为 `localhost` 或 `127.0.0.1`
- 防火墙阻止了端口访问

**解决方案：**
1. 将 `VITE_SERVER_HOST` 设置为 `0.0.0.0`
2. 检查防火墙设置，允许端口访问
3. 确保手机和电脑在同一 WiFi 网络

### 6.2 问题：API 请求失败，出现 CORS 错误

**错误信息：**
```
Access to XMLHttpRequest at 'http://192.168.1.100:8080/api/...' 
from origin 'http://localhost:3001' has been blocked by CORS policy
```

**原因：**
- 代理配置不正确
- `changeOrigin` 未设置为 `true`
- `VITE_APP_BASE_API` 配置错误

**解决方案：**
1. 检查 `.env` 文件中的 `VITE_SERVER_PROXY_TARGET` 是否正确
2. 确保 `vite.config.js` 中 `changeOrigin: true`
3. 确认 `VITE_APP_BASE_API` 与后端 API 路径前缀一致

### 6.3 问题：代理不生效

**原因：**
- 环境变量未正确加载
- 请求路径不匹配代理规则
- 开发服务器未重启

**解决方案：**
1. 检查 `.env` 文件是否在项目根目录
2. 确认环境变量名称以 `VITE_` 开头
3. 重启开发服务器（`npm run dev`）
4. 检查请求路径是否以 `VITE_APP_BASE_API` 开头

### 6.4 问题：后端服务器连接失败

**错误信息：**
```
Error: connect ECONNREFUSED 192.168.1.100:8080
```

**原因：**
- 后端服务器未启动
- IP 地址或端口错误
- 网络不通

**解决方案：**
1. 确认后端服务器已启动
2. 检查后端服务器监听的 IP 和端口
3. 使用 `ping` 或 `telnet` 测试网络连通性
4. 检查后端服务器的防火墙设置

### 6.5 问题：手机访问时 API 请求失败

**原因：**
- 手机访问前端时，API 请求仍然指向 `localhost`
- 代理只在开发服务器端生效，手机直接请求后端会跨域

**解决方案：**
- ✅ **正确做法**：手机访问前端开发服务器（如 `http://192.168.1.50:3001`），前端通过代理转发请求
- ❌ **错误做法**：手机直接访问后端服务器地址

---

## 七、完整配置示例

### 7.1 开发环境配置（支持手机访问）

**文件：`survey-analyst-web/.env.development`**

```bash
# ============================================
# 开发服务器配置
# ============================================
# 开发服务器主机地址
# 0.0.0.0 表示监听所有网络接口，允许局域网设备访问
VITE_SERVER_HOST=0.0.0.0

# 开发服务器端口
VITE_SERVER_PORT=3001

# ============================================
# 代理配置
# ============================================
# 代理目标地址（后端服务地址）
# 格式：http://IP地址:端口
# 示例：
#   - 本地：http://localhost:8080
#   - 局域网：http://192.168.1.100:8080
VITE_SERVER_PROXY_TARGET=http://192.168.1.100:8080

# API 基础路径（代理路径前缀）
# 所有以此路径开头的请求都会被代理到 VITE_SERVER_PROXY_TARGET
VITE_APP_BASE_API=/api

# ============================================
# 应用配置
# ============================================
# 请求超时时间（毫秒）
VITE_APP_TIMEOUT=30000
```

### 7.2 仅本机开发配置

**文件：`survey-analyst-web/.env.development`**

```bash
# 仅本机访问
VITE_SERVER_HOST=localhost
VITE_SERVER_PORT=3001
VITE_SERVER_PROXY_TARGET=http://localhost:8080
VITE_APP_BASE_API=/api
VITE_APP_TIMEOUT=30000
```

## 八、配置检查清单

在配置完成后，请检查以下事项：

- [ ] `.env.development` 文件已创建在项目根目录
- [ ] `VITE_SERVER_PROXY_TARGET` 设置为正确的后端地址
- [ ] `VITE_SERVER_HOST` 根据需求设置（手机访问需设置为 `0.0.0.0`）
- [ ] `VITE_SERVER_PORT` 端口未被占用
- [ ] `VITE_APP_BASE_API` 与后端 API 路径前缀一致
- [ ] 防火墙已允许相应端口访问
- [ ] 后端服务器已启动并可访问
- [ ] 开发服务器已重启以加载新配置

---

## 九、快速测试

### 9.1 测试代理是否生效

1. 启动开发服务器：
   ```bash
   cd survey-analyst-web
   npm run dev
   ```

2. 在浏览器中访问：
   ```
   http://localhost:3001/api/health
   ```
   如果代理配置正确，请求会被转发到 `http://192.168.1.100:8080/api/health`

3. 打开浏览器开发者工具（F12）→ Network 标签，查看请求详情

### 9.2 测试手机访问

1. 确保配置了 `VITE_SERVER_HOST=0.0.0.0`
2. 获取本机 IP 地址（如 `192.168.1.50`）
3. 确保手机和电脑在同一wifi下
4. 在浏览器中输入：`http://192.168.1.50:3001`
5. 如果能看到前端页面，说明配置成功

---

## 十、总结

### 10.1 关键配置点

1. **代理目标地址**：`VITE_SERVER_PROXY_TARGET` 必须指向正确的后端服务器地址
2. **主机地址**：`VITE_SERVER_HOST=0.0.0.0` 才能让手机等设备访问
3. **代理配置**：`changeOrigin: true` 必须设置，避免 CORS 问题

### 10.2 配置原则

- **开发环境**：使用代理解决跨域问题
- **手机调试**：设置 `VITE_SERVER_HOST=0.0.0.0` 并确保防火墙允许访问


