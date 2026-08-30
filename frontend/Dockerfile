# ===== 构建阶段 =====
FROM node:20-alpine AS builder
WORKDIR /app

COPY package*.json ./
# 国内构建加速：npmmirror 镜像（仅容器内构建生效）
RUN npm config set registry https://registry.npmmirror.com && npm ci

COPY . .
RUN npm run build

# ===== 运行阶段：Nginx 托管 dist 并反代后端 =====
FROM nginx:1.27-alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -q --spider http://127.0.0.1/ || exit 1
