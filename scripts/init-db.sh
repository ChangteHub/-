#!/bin/bash
# 本地数据库初始化：创建容器 MySQL + 建库（表结构由后端 Flyway 自动迁移）
set -e

cd "$(dirname "$0")/.."

if [ ! -f .env ]; then
  echo "错误：未找到 .env，请先执行 cp .env.example .env 并填写配置" >&2
  exit 1
fi

# shellcheck disable=SC1091
export $(grep -v '^#' .env | grep MYSQL_ROOT_PASSWORD | xargs)

echo "启动 MySQL 容器..."
docker compose up -d mysql

echo "等待 MySQL 就绪..."
until docker exec secondhand-mysql mysqladmin ping -h 127.0.0.1 -uroot -p"${MYSQL_ROOT_PASSWORD}" --silent 2>/dev/null; do
  sleep 2
done

echo "数据库已就绪：xust_secondhand（表结构与初始数据由后端启动时 Flyway 迁移自动创建）"
