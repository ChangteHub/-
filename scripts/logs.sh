#!/bin/bash
# 快速查看服务日志：bash scripts/logs.sh [mysql|backend|frontend|all]
set -e
cd "$(dirname "$0")/.."

SERVICE="${1:-all}"

case "${SERVICE}" in
  mysql|backend|frontend)
    docker compose logs -f "${SERVICE}"
    ;;
  all)
    docker compose logs -f
    ;;
  *)
    echo "用法：bash scripts/logs.sh [mysql|backend|frontend|all]" >&2
    exit 1
    ;;
esac
