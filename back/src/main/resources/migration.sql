-- 为已有数据库的 user 表添加 role 字段（MySQL 8.0 兼容，幂等）
-- 执行此脚本前请先备份数据库
-- 用法: mysql -uroot -p xust_secondhand < migration.sql

-- MySQL 8.0 不支持 ADD COLUMN IF NOT EXISTS（MariaDB 语法），
-- 这里通过 information_schema 判断列是否存在，再动态执行 ALTER，保证幂等。
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'user'
      AND column_name = 'role'
);

SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE `user` ADD COLUMN `role` tinyint DEFAULT 0 COMMENT ''角色: 0普通用户 1管理员'' AFTER `status`',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 确保 admin_log 表存在（MySQL 8.0 幂等建表）
CREATE TABLE IF NOT EXISTS `admin_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `admin_id` bigint NOT NULL COMMENT '操作管理员ID',
    `admin_username` varchar(50) DEFAULT NULL COMMENT '管理员用户名',
    `action` varchar(50) NOT NULL COMMENT '操作类型',
    `target_type` varchar(30) DEFAULT NULL COMMENT '目标类型: user/product/verification/category',
    `target_id` varchar(30) DEFAULT NULL COMMENT '目标ID',
    `detail` varchar(500) DEFAULT NULL COMMENT '操作详情',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_admin_id` (`admin_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志表';

-- 更新现有用户的 role（可选，取消注释后按需执行）
-- UPDATE `user` SET `role` = 1 WHERE `username` = 'admin';
