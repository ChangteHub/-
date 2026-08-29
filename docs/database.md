# 数据库设计

- 数据库：`xust_secondhand`（utf8mb4 / utf8mb4_unicode_ci，InnoDB）
- Schema 变更**只能**通过新增 Flyway 迁移脚本（`backend/src/main/resources/db/migration/`），禁止手动改库
- 命名 `V{版本}__{描述}.sql`；已执行的脚本不可修改（Flyway 校验 checksum）

## 迁移历史

| 版本 | 内容 |
|------|------|
| V1__init_schema | 10 张表初始结构（含 user.role） |
| V2__init_data | 分类字典、测试账号（test1/test2，密码 115417）、示例商品/会话/收藏 |

## 表清单

| 表 | 说明 | 关键索引 |
|----|------|---------|
| user | 用户（含 role: 0 用户/1 管理员） | uk_username |
| category | 分类字典 | — |
| product | 商品 | idx_seller_id, idx_category_id, idx_status_created |
| product_image | 商品图片 | idx_product_id |
| favorite | 收藏 | uk_user_product |
| chat_session | 会话 | uk_product_buyer_seller, idx_buyer/seller_last_time |
| chat_message | 消息 | idx_session_created |
| browsing_history | 浏览历史 | uk_user_product |
| verification | 实名认证 | idx_user_id |
| admin_log | 管理员操作日志 | idx_admin_id, idx_created_at |

## 存量库接入 Flyway

旧库（曾经手工执行 schema.sql + data.sql）首次接入时：

1. 确保 `user` 表已含 `role` 字段（旧 `migration.sql` 幂等逻辑已并入 V1）
2. 应用启动时 Flyway `baseline-on-migrate=true` + `baseline-version=2` 自动打基线，V1/V2 跳过
3. 此后新变更一律新增 `V3__xxx.sql` 起

## 约定

- 必备字段：`id BIGINT` 主键、`created_at`、`updated_at`（业务表含 `deleted` 逻辑删除位，MyBatis-Plus 已配置）
- 金额用 `DECIMAL(10,2)`；枚举用 `TINYINT` + 注释，不用 ENUM 类型
- 索引遵循最左前缀；单表索引 ≤5 个
