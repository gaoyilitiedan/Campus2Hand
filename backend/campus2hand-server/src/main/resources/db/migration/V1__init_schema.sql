-- ============================================================
-- Campus2Hand 数据库初始化脚本 V1
-- 版本: 1.0.0
-- 说明: 创建全部 15 张核心业务表
-- ============================================================

-- 1. 用户表
CREATE TABLE `users` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    `student_id`        VARCHAR(20)   NOT NULL                 COMMENT '学号',
    `nickname`          VARCHAR(30)   NOT NULL                 COMMENT '昵称',
    `password_hash`     VARCHAR(255)  NOT NULL                 COMMENT '密码哈希(BCrypt)',
    `email`             VARCHAR(100)  NOT NULL                 COMMENT '邮箱',
    `email_verified`    TINYINT(1)    DEFAULT 0                COMMENT '邮箱是否已验证 0-否 1-是',
    `phone`             VARCHAR(20)   DEFAULT NULL             COMMENT '手机号(可选)',
    `avatar_url`        VARCHAR(500)  DEFAULT NULL             COMMENT '头像URL',
    `campus`            VARCHAR(30)   NOT NULL                 COMMENT '所在校区',
    `role`              VARCHAR(20)   NOT NULL DEFAULT 'user'  COMMENT '角色 user/admin',
    `status`            VARCHAR(20)   NOT NULL DEFAULT 'active' COMMENT '状态 active/disabled',
    `reputation_score`  DECIMAL(3,1)  DEFAULT 5.0              COMMENT '信誉分 1.0-5.0',
    `token_version`     INT          DEFAULT 0                COMMENT 'Token版本号，用于强制登出',
    `login_fail_count`  INT          DEFAULT 0                COMMENT '登录失败次数',
    `locked_until`      DATETIME     DEFAULT NULL             COMMENT '锁定截止时间',
    `last_login_at`     DATETIME     DEFAULT NULL             COMMENT '最后登录时间',
    `deleted`           TINYINT(1)    DEFAULT 0                COMMENT '逻辑删除 0-否 1-是',
    `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_id` (`student_id`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_campus` (`campus`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 邮箱验证码表
CREATE TABLE `email_verifications` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    `email`       VARCHAR(100) NOT NULL                 COMMENT '邮箱',
    `code`        VARCHAR(10)  NOT NULL                 COMMENT '验证码',
    `type`        VARCHAR(20)  NOT NULL                 COMMENT '类型 register/reset_password',
    `expires_at`  DATETIME     NOT NULL                 COMMENT '过期时间',
    `used`        TINYINT(1)   DEFAULT 0                COMMENT '是否已使用',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_email_type` (`email`, `type`),
    KEY `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮箱验证码表';

-- 2.1 邮箱验证令牌表
CREATE TABLE `email_tokens` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    `user_id`     BIGINT       NOT NULL                 COMMENT '用户ID',
    `token`       VARCHAR(128) NOT NULL                 COMMENT '验证令牌',
    `type`        VARCHAR(20)  NOT NULL                 COMMENT '类型 verify_email/reset_password',
    `expires_at`  DATETIME     NOT NULL                 COMMENT '过期时间',
    `used`        TINYINT(1)   DEFAULT 0                COMMENT '是否已使用',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token` (`token`),
    KEY `idx_user_id_type` (`user_id`, `type`),
    KEY `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮箱验证令牌表';

-- 3. Refresh Token 表
CREATE TABLE `refresh_tokens` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    `user_id`     BIGINT       NOT NULL                 COMMENT '用户ID',
    `token`       VARCHAR(500) NOT NULL                 COMMENT 'Refresh Token',
    `expires_at`  DATETIME     NOT NULL                 COMMENT '过期时间',
    `revoked`     TINYINT(1)   DEFAULT 0                COMMENT '是否已撤销',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token` (`token`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Refresh Token表';

-- 4. 商品表
CREATE TABLE `products` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '商品ID',
    `user_id`       BIGINT        NOT NULL                 COMMENT '卖家ID',
    `title`         VARCHAR(60)   NOT NULL                 COMMENT '标题',
    `description`   TEXT          NOT NULL                 COMMENT '描述',
    `price`         BIGINT        NOT NULL                 COMMENT '价格（分）',
    `original_price` BIGINT       DEFAULT NULL             COMMENT '原价（分）',
    `category`      VARCHAR(20)   NOT NULL                 COMMENT '分类',
    `item_condition` VARCHAR(20)   NOT NULL                 COMMENT '新旧程度',
    `campus`        VARCHAR(30)   NOT NULL                 COMMENT '所在校区',
    `status`        VARCHAR(20)   NOT NULL DEFAULT 'pending_review' COMMENT '状态 on_sale/pending_review/sold/delisted',
    `view_count`    INT           DEFAULT 0                COMMENT '浏览量',
    `favorite_count` INT          DEFAULT 0                COMMENT '收藏数',
    `version`       INT           DEFAULT 0                COMMENT '乐观锁版本号',
    `deleted`       TINYINT(1)    DEFAULT 0                COMMENT '逻辑删除 0-否 1-是',
    `audit_reason`  VARCHAR(500)  DEFAULT NULL             COMMENT '审核驳回原因',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id_status` (`user_id`, `status`),
    KEY `idx_status_created_at` (`status`, `created_at`),
    KEY `idx_campus_category` (`campus`, `category`),
    KEY `idx_price` (`price`),
    FULLTEXT KEY `ft_title_description` (`title`, `description`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 5. 商品图片表
CREATE TABLE `product_images` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '图片ID',
    `product_id`  BIGINT       NOT NULL                 COMMENT '商品ID',
    `url`         VARCHAR(500) NOT NULL                 COMMENT '图片URL',
    `sort_order`  INT          DEFAULT 0                COMMENT '排序',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品图片表';

-- 6. 收藏表
CREATE TABLE `favorites` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    `user_id`     BIGINT   NOT NULL                 COMMENT '用户ID',
    `product_id`  BIGINT   NOT NULL                 COMMENT '商品ID',
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- 7. 订单表
CREATE TABLE `orders` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '订单ID',
    `order_no`          VARCHAR(32)  NOT NULL                 COMMENT '订单编号',
    `buyer_id`          BIGINT       NOT NULL                 COMMENT '买家ID',
    `seller_id`         BIGINT       NOT NULL                 COMMENT '卖家ID',
    `product_id`        BIGINT       NOT NULL                 COMMENT '商品ID',
    `amount`            BIGINT       NOT NULL                 COMMENT '订单金额（分）',
    `status`            VARCHAR(20)  NOT NULL DEFAULT 'pending_payment' COMMENT '状态 pending_payment/paid_escrow/pending_shipment/completed/cancelled/refunding/refunded/settlement_failed',
    `payment_method`    VARCHAR(20)  DEFAULT NULL             COMMENT '支付方式 wechat',
    `transaction_id`    VARCHAR(64)  DEFAULT NULL             COMMENT '微信交易号',
    `refund_id`         VARCHAR(64)  DEFAULT NULL             COMMENT '微信退款号',
    `refund_amount`     BIGINT       DEFAULT NULL             COMMENT '退款金额（分）',
    `refund_reason`     VARCHAR(500) DEFAULT NULL             COMMENT '退款原因',
    `refund_requested_at` DATETIME   DEFAULT NULL             COMMENT '退款申请时间',
    `refund_completed_at` DATETIME   DEFAULT NULL             COMMENT '退款完成时间',
    `paid_at`           DATETIME     DEFAULT NULL             COMMENT '支付时间',
    `completed_at`      DATETIME     DEFAULT NULL             COMMENT '完成时间',
    `cancelled_at`      DATETIME     DEFAULT NULL             COMMENT '取消时间',
    `expires_at`        DATETIME     NOT NULL                 COMMENT '支付过期时间(30min)',
    `version`           INT          DEFAULT 0                COMMENT '乐观锁版本号',
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_buyer_id_status` (`buyer_id`, `status`),
    KEY `idx_seller_id_status` (`seller_id`, `status`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 8. 会话表
CREATE TABLE `conversations` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '会话ID',
    `product_id`      BIGINT       NOT NULL                 COMMENT '关联商品ID',
    `buyer_id`        BIGINT       NOT NULL                 COMMENT '买家ID',
    `seller_id`       BIGINT       NOT NULL                 COMMENT '卖家ID',
    `last_message`    VARCHAR(500) DEFAULT NULL             COMMENT '最后一条消息摘要',
    `last_message_at` DATETIME     DEFAULT NULL             COMMENT '最后消息时间',
    `buyer_unread`    INT          DEFAULT 0                COMMENT '买家未读数',
    `seller_unread`   INT          DEFAULT 0                COMMENT '卖家未读数',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_buyer` (`product_id`, `buyer_id`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_seller_id` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- 9. 消息表
CREATE TABLE `messages` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '消息ID',
    `conversation_id` BIGINT       NOT NULL                 COMMENT '会话ID',
    `sender_id`       BIGINT       NOT NULL                 COMMENT '发送者ID',
    `receiver_id`     BIGINT       NOT NULL                 COMMENT '接收者ID',
    `content`         VARCHAR(1000) NOT NULL                COMMENT '消息内容',
    `type`            VARCHAR(20)  NOT NULL DEFAULT 'text'  COMMENT '消息类型 text/image',
    `is_read`         TINYINT(1)   DEFAULT 0                COMMENT '是否已读',
    `read_at`         DATETIME     DEFAULT NULL             COMMENT '已读时间',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id_created` (`conversation_id`, `created_at`),
    KEY `idx_receiver_read` (`receiver_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- 10. 评价表
CREATE TABLE `reviews` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '评价ID',
    `order_id`    BIGINT       NOT NULL                 COMMENT '订单ID',
    `reviewer_id` BIGINT       NOT NULL                 COMMENT '评价者ID',
    `target_id`   BIGINT       NOT NULL                 COMMENT '被评价者ID',
    `rating`      TINYINT      NOT NULL                 COMMENT '评分 1-5',
    `content`     VARCHAR(500) DEFAULT NULL             COMMENT '评价内容',
    `tags`        VARCHAR(200) DEFAULT NULL             COMMENT '评价标签 JSON数组',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_reviewer` (`order_id`, `reviewer_id`),
    KEY `idx_target_id` (`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- 11. 纠纷/工单表
CREATE TABLE `disputes` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '工单ID',
    `order_id`        BIGINT       NOT NULL                 COMMENT '关联订单ID',
    `reporter_id`     BIGINT       NOT NULL                 COMMENT '举报人ID',
    `type`            VARCHAR(20)  NOT NULL                 COMMENT '类型 refund_dispute/report',
    `reason`          VARCHAR(500) NOT NULL                 COMMENT '原因',
    `evidence_urls`   TEXT         DEFAULT NULL             COMMENT '证据图片URL JSON数组',
    `status`          VARCHAR(20)  NOT NULL DEFAULT 'pending' COMMENT '状态 pending/processing/resolved/closed',
    `resolution`      VARCHAR(500) DEFAULT NULL             COMMENT '处理结果',
    `resolved_by`     BIGINT       DEFAULT NULL             COMMENT '处理人ID',
    `resolved_at`     DATETIME     DEFAULT NULL             COMMENT '处理时间',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='纠纷/工单表';

-- 12. 通知表
CREATE TABLE `notifications` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '通知ID',
    `user_id`     BIGINT       NOT NULL                 COMMENT '接收用户ID',
    `type`        VARCHAR(30)  NOT NULL                 COMMENT '通知类型 message/order_status/refund/dispute/system',
    `title`       VARCHAR(100) NOT NULL                 COMMENT '通知标题',
    `content`     VARCHAR(500) NOT NULL                 COMMENT '通知内容',
    `target_type` VARCHAR(20)  DEFAULT NULL             COMMENT '跳转目标类型 order/conversation/dispute',
    `target_id`   BIGINT       DEFAULT NULL             COMMENT '跳转目标ID',
    `is_read`     TINYINT(1)   DEFAULT 0                COMMENT '是否已读 0-否 1-是',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_read_time` (`user_id`, `is_read`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内通知表';

-- 13. 敏感词表
CREATE TABLE `sensitive_words` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    `word`       VARCHAR(50)  NOT NULL                 COMMENT '敏感词',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词表';

-- 14. 商品审核标签表
CREATE TABLE `review_tags` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    `product_id` BIGINT       NOT NULL                 COMMENT '商品ID',
    `tag`        VARCHAR(50)  NOT NULL                 COMMENT '审核标签',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品审核标签表';

-- 15. 管理员操作日志表
CREATE TABLE `admin_logs` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    `admin_id`    BIGINT       NOT NULL                 COMMENT '管理员ID',
    `action`      VARCHAR(50)  NOT NULL                 COMMENT '操作类型',
    `target_type` VARCHAR(30)  NOT NULL                 COMMENT '操作对象类型',
    `target_id`   BIGINT       NOT NULL                 COMMENT '操作对象ID',
    `detail`      VARCHAR(1000) DEFAULT NULL            COMMENT '操作详情',
    `ip`          VARCHAR(50)  DEFAULT NULL             COMMENT '操作IP',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_admin_id` (`admin_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志表';

-- 16. 登录历史表
CREATE TABLE `login_history` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    `user_id`     BIGINT       NOT NULL                 COMMENT '用户ID',
    `login_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    `ip`          VARCHAR(50)  DEFAULT NULL             COMMENT '登录IP',
    `device`      VARCHAR(200) DEFAULT NULL             COMMENT '登录设备',
    PRIMARY KEY (`id`),
    KEY `idx_user_id_login_at` (`user_id`, `login_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录历史表';