# 部署指南

## 环境分级

| 环境 | 方式 | Profile |
|------|------|---------|
| dev（本地） | 裸机或 Docker Compose | `dev`（默认） |
| prod（单台 VPS） | Docker Compose + Nginx | `prod` |

## 一键启动（Docker Compose）

```bash
# 1. 准备环境变量（必需：MYSQL_ROOT_PASSWORD、JWT_SECRET）
cp .env.example .env
# 编辑 .env，填入强密码与强随机 JWT_SECRET（openssl rand -base64 48）

# 2. 构建并启动（MySQL + 后端 + Nginx）
docker compose up -d --build

# 3. 验证
curl http://localhost/                      # 前端页面
curl http://localhost/health                # 后端健康检查（actuator 代理）
docker compose ps
```

- Nginx（frontend 容器）是唯一公网入口：静态托管 `dist/` + 反代 `/api`、`/uploads`、`/ws`
- backend / mysql 只绑定 `127.0.0.1`，不暴露公网
- 首次启动 Flyway 自动建表灌数据；管理员账号由 DataInitializer 创建（密码来自 `ADMIN_PASSWORD`，未设置则随机打印到启动日志）

## 生产加固清单（发布前逐项确认）

1. **HTTPS**：域名解析到服务器后，用 certbot 签发证书，Nginx 增加 443 server block + HTTP 301 跳转
   ```bash
   apt install certbot python3-certbot-nginx -y
   certbot --nginx -d your-domain.com
   ```
2. **防火墙**：只开 22/80/443，3306/8080 一律不暴露
3. **强密钥**：`.env` 中 `JWT_SECRET`、`MYSQL_ROOT_PASSWORD` 必须为强随机值
4. **备份**：配置每日 `mysqldump`（见 `scripts/backup.sh`），发布前先备份
5. **数据库变更**：只通过新增 Flyway 迁移文件，随镜像发布自动执行

## 回滚

```bash
# 镜像按语义化 tag 保留，回退即改 tag 重新 up
docker compose down
# 编辑 docker-compose.yml 中 image tag 到上一版本（如 secondhand-backend:1.0.0 → 旧 tag）
docker compose up -d
# 数据库恢复：gunzip < backup-YYYYMMDD.sql.gz | docker exec -i secondhand-mysql mysql -uroot -p xust_secondhand
```

## CI/CD

- 现状：`.github/workflows/ci.yml` 只做 **test + build**（无自动部署）
- 后续扩展 deploy job 需要：服务器 SSH 私钥、镜像仓库凭据等 GitHub Secrets，参考 Skill cicd.md 的 deploy job 模板（SSH → pull 新镜像 → 重启容器 → health check）

## 部署后验证

1. `curl -I http://<域名>` 首页 200
2. 子路由刷新不 404（`try_files` 生效）
3. `curl http://<域名>/api/category/list` 返回 `{"code":200,...}`
4. WebSocket 聊天可连接（wss）
5. 图片上传 ≤10MB 成功
