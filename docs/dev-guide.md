# 开发指南

## 环境要求

- JDK 21、Maven 3.9+
- Node.js 20+（本机 24 亦可）
- MySQL 8（或直接用 Docker Compose 起依赖）
- Docker + Docker Compose（可选，推荐）

## 本地开发（方式一：Docker Compose 全套）

```bash
cp .env.example .env   # 填入密码/JWT_SECRET
make dev               # MySQL + 后端 + Nginx 一键启动
```

## 本地开发（方式二：裸机前后端分离）

```bash
# 1. 准备数据库（只建库，表由 Flyway 自动迁移）
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS xust_secondhand DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 启动后端（dev profile，可用环境变量覆盖默认连接）
cd backend
export DB_PASSWORD=your-db-password        # 或使用 dev 默认 root123
export JWT_SECRET=dev-only-jwt-secret-change-me-38bytes!
mvn spring-boot:run

# 3. 启动前端（Vite dev server，/api /uploads /ws 自动代理到 8080）
cd frontend
npm install
npm run dev
```

访问：
- 前端 http://localhost:3000
- 接口文档（Knife4j，仅 dev）http://localhost:8080/doc.html
- 管理后台 http://localhost:3000/admin

## 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| test1 | 115417 | 普通用户 |
| test2 | 115417 | 普通用户 |
| admin | 来自 `ADMIN_PASSWORD` 环境变量，或首次启动日志打印的随机密码 | 管理员 |

## 常用命令

| 命令 | 说明 |
|------|------|
| `make build` | 构建前端 dist + 后端 jar |
| `make test` | 前端 Vitest + 后端 JUnit 全量测试 |
| `make logs` | 跟踪容器日志 |
| `make db-init` | 起容器 MySQL 并等待就绪 |

## 编码规范

- **提交信息**：Conventional Commits（`feat:` `fix:` `docs:` `refactor:` `chore:` `test:`）
- **前端**：页面放 `pages/`；所有 HTTP 请求必须走 `services/`（禁止页面直接 import axios）；新代码用 `@/` 路径别名
- **后端**：Controller 不写业务；事务只加在 Service 层；DTO 命名 `XxxRequest` / `XxxResponse`
- **数据库**：任何 schema 变更 = 新增一个 Flyway 迁移文件，不改已执行脚本
- **密钥**：一律环境变量注入，不硬编码、不提交 `.env`
