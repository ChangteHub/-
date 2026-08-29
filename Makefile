.PHONY: help dev backend frontend build test clean db-init logs up down

# 默认目标：显示帮助
help:
	@echo "可用命令："
	@echo "  make dev        - 一键启动完整环境（MySQL + 后端 + Nginx，需先 cp .env.example .env）"
	@echo "  make backend    - 本地裸机启动后端（需本机 MySQL，默认 dev profile）"
	@echo "  make frontend   - 启动前端开发服务"
	@echo "  make build      - 构建前后端产物"
	@echo "  make test       - 运行全部测试（前端 Vitest + 后端 JUnit）"
	@echo "  make db-init    - 初始化本地数据库（创建库，表结构由 Flyway 自动迁移）"
	@echo "  make logs       - 查看容器日志"
	@echo "  make up / down  - 启动 / 停止容器编排"
	@echo "  make clean      - 清理构建产物"

dev: up
	@echo "启动完成：前端 http://localhost  接口文档(仅dev) http://localhost:8080/doc.html"

backend:
	cd backend && mvn spring-boot:run

frontend:
	cd frontend && npm install && npm run dev

build:
	cd frontend && npm ci && npm run build
	cd backend && mvn clean package -DskipTests

test:
	cd frontend && npm run test:run
	cd backend && mvn test

db-init:
	docker compose up -d mysql
	@echo "等待 MySQL 就绪..."
	until docker exec secondhand-mysql mysqladmin ping -h 127.0.0.1 -uroot -p$$(grep MYSQL_ROOT_PASSWORD .env | cut -d= -f2) --silent 2>/dev/null; do sleep 2; done
	@echo "数据库已就绪（建表由后端 Flyway 自动执行）"

logs:
	docker compose logs -f

up:
	docker compose up -d --build

down:
	docker compose down

clean:
	docker compose down -v
	rm -rf frontend/dist backend/target
