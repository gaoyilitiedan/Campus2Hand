-- ============================================================
-- Campus2Hand 种子数据 V2
-- 说明: 插入初始敏感词库和测试管理员账号
-- ============================================================

INSERT INTO `sensitive_words` (`word`) VALUES
('枪支'), ('弹药'), ('毒品'), ('色情'), ('赌博'),
('假币'), ('发票'), ('代办'), ('代考'), ('替考'),
('作弊'), ('窃听'), ('偷拍'), ('迷药'), ('管制刀具');

INSERT INTO `users` (`id`, `student_id`, `nickname`, `password_hash`, `email`, `email_verified`, `campus`, `role`, `status`)
VALUES (1, 'admin001', '系统管理员', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', 'admin@campus2hand.com', 1, '主校区', 'admin', 'active');