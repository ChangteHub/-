#!/bin/bash
# deploy.sh — 生产环境一键更新（在服务器 /opt/secondhand 下运行）
# 用法: bash deploy.sh
# 流程: git 拉取最新代码（重试 3 次）→ 有更新则重建容器 → 健康检查
# 依赖: 服务器能访问 GitHub（时通时断时，可用本地 git bundle 桥:
#       本地: git bundle create p1.bundle main && scp p1.bundle root@服务器:/tmp/
#       服务器: git fetch /tmp/p1.bundle main && git reset --hard FETCH_HEAD && bash deploy.sh）
set -euo pipefail
cd "${SECONDHAND_DIR:-/opt/secondhand}"

OLD=$(git rev-parse HEAD)
echo "当前版本: $(git log --oneline -1)"

ok=0
for i in 1 2 3; do
  if timeout 40 git fetch origin main 2>/dev/null; then ok=1; break; fi
  echo "GitHub 连接失败（第 $i/3 次），5 秒后重试..."
  sleep 5
done
if [ "$ok" -ne 1 ]; then
  echo "GitHub 不可达：改用本地 git bundle 桥更新（见文件头注释），或稍后重试" >&2
  exit 1
fi

NEW=$(git rev-parse FETCH_HEAD)
if [ "$OLD" = "$NEW" ]; then
  echo "已是最新（$NEW），无需重建"
  exit 0
fi

echo "更新: $OLD -> $NEW"
git reset --hard FETCH_HEAD
docker compose up -d --build

echo "等待容器就绪..."
sleep 25
if curl -sf http://localhost/health >/dev/null; then
  echo "✅ 部署完成且健康: $(git log --oneline -1)"
else
  echo "⚠️ 容器已启动但健康检查未就绪，稍后复查: curl http://localhost/health" >&2
  docker compose ps
  exit 1
fi
