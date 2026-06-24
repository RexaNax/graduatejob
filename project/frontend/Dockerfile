# ============================================
# 前端 Dockerfile - 多阶段构建
# ============================================
# 阶段1: 构建阶段 - 使用 Node.js 编译前端
# 阶段2: 运行阶段 - 使用 Nginx 提供静态文件服务
# ============================================

# ========== 阶段1: 构建 ==========
FROM node:18-alpine AS builder

# 设置工作目录
WORKDIR /app

# 先复制 package.json，利用 Docker 缓存层
COPY package*.json ./

# 安装依赖
RUN npm install --registry=https://registry.npmmirror.com

# 复制源代码
COPY . .

# 构建生产版本
RUN npm run build

# ========== 阶段2: 运行 ==========
FROM nginx:alpine

# 设置时区
ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/$TZ /etc/localtime

# 删除默认配置
RUN rm -rf /usr/share/nginx/html/*

# 从构建阶段复制静态文件
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制自定义 nginx 配置（会在 docker-compose 中挂载）
# 这里先用默认配置，实际配置通过挂载覆盖

# 暴露端口
EXPOSE 80

# 启动 Nginx
CMD ["nginx", "-g", "daemon off;"]
