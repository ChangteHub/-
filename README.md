# 西南科技大学校园二手交易平台

面向西南科技大学在校学生的 C2C 二手物品交易平台。卖家发布商品，买家通过站内聊天联系，线下校内交易。

## 技术栈

- **前端**：React 18 + TypeScript + Vite 6 + Ant Design Mobile + Zustand + Axios
- **后端**：Java 21 + Spring Boot 3.2.5 + MyBatis-Plus + Spring Security/JWT + WebSocket
- **数据库**：MySQL 8.0 + Flyway 版本化迁移
- **部署**：Docker Compose + Nginx（唯一公网入口）
- **CI**：GitHub Actions（测试 + 构建）

## 快速开始

### 前置要求

- Docker & Docker Compose（推荐），或 JDK 21 + Maven 3.9+ / Node 20+ 裸机开发
- 本机开发详见 [docs/dev-guide.md](docs/dev-guide.md)

### 一键启动（Docker Compose）

```bash
# 1. 配置环境变量（必填：MYSQL_ROOT_PASSWORD、JWT_SECRET）
cp .env.example .env

# 2. 构建并启动 MySQL + 后端 + Nginx
docker compose up -d --build

# 3. 验证
curl http://localhost/health   # 后端健康检查
```

前端 http://localhost · 管理后台 http://localhost/admin

首次启动自动完成：Flyway 建表灌数据 → 创建管理员账号（密码来自 `.env` 的 `ADMIN_PASSWORD`，未设置则随机生成并打印在后端启动日志）。

### 裸机开发（前后端分离）

```bash
# 后端（dev profile，默认连接 localhost:3306，可用环境变量覆盖）
cd backend && mvn spring-boot:run

# 前端（/api、/uploads、/ws 自动代理到 8080）
cd frontend && npm install && npm run dev
```

访问：前端 http://localhost:3000 · 接口文档 http://localhost:8080/doc.html

### 测试账号

| 用户名 | 密码 | 说明 |
|--------|------|------|
| test1 | 115417 | 普通用户（Flyway V2 初始化） |
| test2 | 115417 | 普通用户 |
| admin | 见 `ADMIN_PASSWORD` 或启动日志 | 管理员（DataInitializer 创建） |

## 项目结构

```
project1/
├── frontend/                  # React 前端
│   ├── src/services/          # HTTP 通信层（request.ts + 按业务域拆分模块）
│   ├── src/stores/            # Zustand 全局状态
│   ├── src/pages/             # 16 个业务页面（含 /admin）
│   ├── nginx.conf             # 容器 Nginx 配置（静态托管 + API 反代）
│   └── Dockerfile
├── backend/                   # Spring Boot 单体
│   ├── src/main/java/com/xust/secondhand/
│   │   ├── controller/ service/ repository/ entity/
│   │   ├── dto/request/ dto/response/   # 请求/响应 DTO
│   │   ├── security/ exception/ config/ common/ websocket/
│   ├── src/main/resources/db/migration/ # Flyway 迁移（V1 表结构 / V2 初始数据）
│   └── Dockerfile             # 多阶段构建（Maven build → JRE runtime）
├── docs/                      # architecture / database / deployment / dev-guide
├── scripts/                   # init-db.sh / backup.sh / logs.sh
├── .github/workflows/ci.yml   # CI（测试 + 构建）
├── docker-compose.yml         # MySQL + backend(127.0.0.1) + Nginx(80)
├── .env.example               # 环境变量模板（.env 不入 Git）
└── Makefile                   # make dev / build / test / logs
```

架构细节见 [docs/architecture.md](docs/architecture.md)，数据库设计见 [docs/database.md](docs/database.md)。

## API 概览

完整接口文档（dev 环境）：<http://localhost:8080/doc.html>

| 模块 | 前缀 |
|------|------|
| 认证 | `/api/auth`（注册/登录/me） |
| 商品 | `/api/product`、`/api/category`、`/api/search` |
| 用户 | `/api/user`、`/api/favorite`、`/api/history`、`/api/verification` |
| 聊天 | `/api/chat` + WebSocket `/ws/chat` |
| 管理员 | `/api/admin`（RBAC，需 ADMIN 角色） |

统一响应契约：`{code, message, data}`；401 未认证 / 403 无权限 / 404 不存在 / 500 服务异常。

## 功能特性

- 用户注册/登录（JWT）、实名认证（管理员审核）
- 商品发布/编辑/上下架、分类、搜索、排序、浏览历史
- 收藏、站内聊天（WebSocket 实时）
- 管理后台：用户/商品/认证/分类管理与操作日志
- 深色模式、移动端响应式

## 部署

单台 VPS 生产部署（HTTPS、防火墙、备份、回滚）见 [docs/deployment.md](docs/deployment.md)。

## 贡献

提交信息遵循 [Conventional Commits](https://www.conventionalcommits.org/)（`feat:` `fix:` `docs:` `refactor:` `chore:`）。提交前确保 `make test` 通过。

## 许可证

MIT License
