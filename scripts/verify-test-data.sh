#!/bin/bash
# verify-test-data.sh — 校验种子测试数据就位（smoke test 前置检查）
# 用法: bash verify-test-data.sh [项目根路径]
# 退出码: 0 = 种子数据就位；1 = 缺失（先跑 seed-test-data.sh）
set -euo pipefail

ROOT="${1:-.}"
ROOT="$(cd "$ROOT" && pwd)"
[ -f "$ROOT/.env" ] || { echo "FAIL verify-test-data（无 .env）" >&2; exit 1; }
# shellcheck disable=SC1091
export $(grep -v '^#' "$ROOT/.env" | grep -E 'MYSQL_ROOT_PASSWORD|MYSQL_DATABASE|MYSQL_CONTAINER' | xargs) 2>/dev/null || true

CONTAINER="${MYSQL_CONTAINER:-secondhand-mysql}"
DB="${MYSQL_DATABASE:-xust_secondhand}"
docker ps --format '{{.Names}}' | grep -qx "$CONTAINER" || { echo "FAIL verify-test-data（容器未运行）" >&2; exit 1; }

RESULT=$(docker exec -i "$CONTAINER" mysql --default-character-set=utf8mb4 -uroot -p"${MYSQL_ROOT_PASSWORD}" "$DB" -N -e "
SELECT CONCAT(
  (SELECT COUNT(*) FROM \`user\` WHERE username IN ('test1','test2') AND deleted=0), '/',
  (SELECT COUNT(*) FROM \`category\` WHERE status=0))
" 2>/dev/null) || RESULT="0/0"

SEED_USERS="${RESULT%%/*}"
CATEGORIES="${RESULT##*/}"

echo "种子用户: $SEED_USERS/2   分类字典: $CATEGORIES"
if [ "$SEED_USERS" = "2" ] && [ "${CATEGORIES:-0}" -ge 1 ]; then
  echo "PASS  verify-test-data"
  exit 0
fi
echo "FAIL  verify-test-data —— 请先运行 bash scripts/seed-test-data.sh"
exit 1
