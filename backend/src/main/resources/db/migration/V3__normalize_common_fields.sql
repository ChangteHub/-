-- V3: 为早期建表的 7 张表补齐统一的 updated_at / deleted 字段
-- （与 skill 数据库规范的必备字段对齐；均为可空/带默认值的加列操作，对现有数据与代码无行为影响）
ALTER TABLE `admin_log`       ADD COLUMN `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `chat_message`    ADD COLUMN `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `chat_session`    ADD COLUMN `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `product_image`   ADD COLUMN `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `admin_log`       ADD COLUMN `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除';
ALTER TABLE `browsing_history` ADD COLUMN `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除';
ALTER TABLE `chat_message`    ADD COLUMN `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除';
ALTER TABLE `chat_session`    ADD COLUMN `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除';
ALTER TABLE `favorite`        ADD COLUMN `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除';
ALTER TABLE `product_image`   ADD COLUMN `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除';
ALTER TABLE `verification`    ADD COLUMN `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除';
