#!/bin/bash
# 数据库备份：mysqldump 导出并 gzip，保留在 ./backups/
set -e

cd "$(dirname "$0")/.."

if [ ! -f .env ]; then
  echo "错误：未找到 .env" >&2
  exit 1
fi

# shellcheck disable=SC1091
export $(grep -v '^#' .env | grep MYSQL_ROOT_PASSWORD | xargs)

BACKUP_DIR="./backups"
mkdir -p "${BACKUP_DIR}"
STAMP=$(date +%Y%m%d-%H%M%S)
FILE="${BACKUP_DIR}/xust_secondhand-${STAMP}.sql.gz"

echo "备份数据库 -> ${FILE}"
docker exec secondhand-mysql mysqldump -uroot -p"${MYSQL_ROOT_PASSWORD}" \
  --single-transaction --routines --triggers xust_secondhand | gzip > "${FILE}"

echo "完成：$(du -h "${FILE}" | cut -f1)"
echo "恢复命令：gunzip < ${FILE} | docker exec -i secondhand-mysql mysql -uroot -p\"\$MYSQL_ROOT_PASSWORD\" xust_secondhand"

# 可选：cron 每日备份
#   0 3 * * * cd /opt/secondhand && bash scripts/backup.sh >> backups/backup.log 2>&1
