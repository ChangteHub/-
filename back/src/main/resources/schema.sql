-- 创建数据库
-- ⚠️ 本脚本仅供【新库】初始化使用，包含 DROP TABLE，会清空同库已有数据！
-- 旧库升级请使用 migration.sql（幂等）
CREATE DATABASE IF NOT EXISTS xust_secondhand DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE xust_secondhand;

-- 1. 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` varchar(50) NOT NULL COMMENT '用户名/学号',
    `password` varchar(100) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
    `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `school` varchar(100) DEFAULT '西南科技大学' COMMENT '学校',
    `student_id` varchar(20) DEFAULT NULL COMMENT '学号',
    `bio` varchar(255) DEFAULT NULL COMMENT '个人简介',
    `status` tinyint DEFAULT 0 COMMENT '状态: 0正常 1禁用',
    `role` tinyint DEFAULT 0 COMMENT '角色: 0普通用户 1管理员',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 分类表
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` varchar(50) NOT NULL COMMENT '分类名称',
    `icon` varchar(255) DEFAULT NULL COMMENT '图标URL',
    `sort` int DEFAULT 0 COMMENT '排序',
    `status` tinyint DEFAULT 0 COMMENT '状态: 0启用 1禁用',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- 3. 商品表
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `seller_id` bigint NOT NULL COMMENT '卖家用户ID',
    `category_id` bigint DEFAULT NULL COMMENT '分类ID',
    `title` varchar(100) NOT NULL COMMENT '商品标题',
    `description` text COMMENT '商品描述',
    `price` decimal(10,2) NOT NULL COMMENT '出售价格',
    `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
    `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图URL',
    `product_condition` varchar(20) DEFAULT NULL COMMENT '商品成色: 全新/九成新/八成新/七成新及以下',
    `location` varchar(100) DEFAULT '西南科技大学' COMMENT '交易地点',
    `status` tinyint DEFAULT 0 COMMENT '状态: 0在售 1已售出 2已下架',
    `view_count` int DEFAULT 0 COMMENT '浏览量',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_status_created` (`status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 4. 商品图片表
DROP TABLE IF EXISTS `product_image`;
CREATE TABLE `product_image` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `image_url` varchar(255) NOT NULL COMMENT '图片URL',
    `sort` int DEFAULT 0 COMMENT '排序',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品图片表';

-- 5. 会话表
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `product_id` bigint DEFAULT NULL COMMENT '关联商品ID',
    `buyer_id` bigint NOT NULL COMMENT '买家ID',
    `seller_id` bigint NOT NULL COMMENT '卖家ID',
    `last_message` varchar(500) DEFAULT NULL COMMENT '最后一条消息摘要',
    `last_message_time` datetime DEFAULT NULL COMMENT '最后消息时间',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_buyer_seller` (`product_id`, `buyer_id`, `seller_id`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_buyer_last_time` (`buyer_id`, `last_message_time`),
    KEY `idx_seller_last_time` (`seller_id`, `last_message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- 6. 聊天消息表
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `session_id` bigint NOT NULL COMMENT '会话ID',
    `sender_id` bigint NOT NULL COMMENT '发送者ID',
    `content` text NOT NULL COMMENT '消息内容',
    `type` tinyint DEFAULT 0 COMMENT '消息类型: 0文字 1图片',
    `is_read` tinyint DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_session_created` (`session_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- 7. 收藏表
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- 8. 浏览历史表
DROP TABLE IF EXISTS `browsing_history`;
CREATE TABLE `browsing_history` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `viewed_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='浏览历史表';

-- 9. 实名认证表
DROP TABLE IF EXISTS `verification`;
CREATE TABLE `verification` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '认证ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
    `student_id` varchar(20) NOT NULL COMMENT '学号',
    `college` varchar(100) DEFAULT NULL COMMENT '学院',
    `enroll_year` int DEFAULT NULL COMMENT '入学年份',
    `student_card_url` varchar(255) NOT NULL COMMENT '学生证照片URL',
    `status` tinyint DEFAULT 0 COMMENT '状态: 0待审核 1已通过 2已拒绝',
    `reject_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实名认证表';

-- 10. 管理员操作日志表
DROP TABLE IF EXISTS `admin_log`;
CREATE TABLE `admin_log` (
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
