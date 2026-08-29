
-- 初始化分类数据
INSERT INTO `category` (`name`, `icon`, `sort`, `status`) VALUES
('教材书籍', 'BookOutlined', 1, 0),
('数码电子', 'MobileOutlined', 2, 0),
('生活用品', 'HomeOutlined', 3, 0),
('服饰鞋包', 'ShoppingOutlined', 4, 0),
('运动户外', 'TrophyOutlined', 5, 0),
('美妆护肤', 'SmileOutlined', 6, 0),
('乐器周边', 'CustomerServiceOutlined', 7, 0),
('其他', 'EllipsisOutlined', 8, 0);

-- 初始化测试用户 (密码: 115417, BCrypt加密)
-- BCrypt哈希由bcryptjs生成并验证, Spring Security BCryptPasswordEncoder兼容$2b$前缀
-- 注：admin 账号不在此处初始化，由 DataInitializer 按 ADMIN_PASSWORD 环境变量（或随机生成）自动创建
INSERT INTO `user` (`username`, `password`, `nickname`, `avatar`, `phone`, `school`, `student_id`, `bio`, `role`) VALUES
('test1', '$2b$10$O8In8FZQAvU6CfXqkPX2J.s1ui/NfKTgBM1ELbYAjnSrxCEUfuMSO', '测试用户1', NULL, '13800138001', '西南科技大学', '2024001', '大三计算机专业', 0),
('test2', '$2b$10$O8In8FZQAvU6CfXqkPX2J.s1ui/NfKTgBM1ELbYAjnSrxCEUfuMSO', '测试用户2', NULL, '13800138002', '西南科技大学', '2024002', '大四机械专业', 0);

-- 初始化测试商品
INSERT INTO `product` (`seller_id`, `category_id`, `title`, `description`, `price`, `original_price`, `cover_image`, `location`, `product_condition`, `status`) VALUES
(1, 1, '高等数学同济第七版 上下册', '大一用过一学期，内页有少量笔记，不影响使用。上下册打包出。', 25.00, 68.00, NULL, '西南科技大学新区', '九成新', 0),
(1, 2, 'AirPods Pro 2代 有发票', '去年双十一买的，用了一年，功能完好，有发票可验。', 980.00, 1899.00, NULL, '西南科技大学老区', '九成新', 0),
(2, 3, '小米台灯 护眼灯 95新', '毕业清宿舍，台灯功能正常，三档调光。', 45.00, 129.00, NULL, '西南科技大学新区', '九成新', 0);

-- 初始化测试会话
INSERT INTO `chat_session` (`product_id`, `buyer_id`, `seller_id`, `last_message`, `last_message_time`) VALUES
(1, 2, 1, '高数书还能便宜点吗？', '2026-08-17 14:30:00');

-- 初始化测试消息
INSERT INTO `chat_message` (`session_id`, `sender_id`, `content`, `type`, `is_read`, `created_at`) VALUES
(1, 2, '你好，高数书还在吗？', 0, 1, '2026-08-17 14:00:00'),
(1, 1, '在的，要看实物图吗？', 0, 1, '2026-08-17 14:05:00'),
(1, 2, '嗯，发几张看看', 0, 1, '2026-08-17 14:10:00');

-- 初始化测试收藏
INSERT INTO `favorite` (`user_id`, `product_id`) VALUES
(2, 1),
(2, 2);
