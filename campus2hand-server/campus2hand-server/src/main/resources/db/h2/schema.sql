-- ============================================================
-- Campus2Hand H2 数据库初始化脚本 (开发环境)
-- ============================================================

-- 1. 用户表
CREATE TABLE users (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    student_id        VARCHAR(20)   NOT NULL,
    nickname          VARCHAR(30)   NOT NULL,
    password_hash     VARCHAR(255)  NOT NULL,
    email             VARCHAR(100)  NOT NULL,
    email_verified    BOOLEAN       DEFAULT FALSE,
    phone             VARCHAR(20)   DEFAULT NULL,
    avatar_url        VARCHAR(500)  DEFAULT NULL,
    campus            VARCHAR(30)   NOT NULL,
    role              VARCHAR(20)   NOT NULL DEFAULT 'user',
    status            VARCHAR(20)   NOT NULL DEFAULT 'active',
    reputation_score  DECIMAL(3,1)  DEFAULT 5.0,
    token_version     INT           DEFAULT 0,
    login_fail_count  INT           DEFAULT 0,
    locked_until      TIMESTAMP     DEFAULT NULL,
    last_login_at     TIMESTAMP     DEFAULT NULL,
    deleted           BOOLEAN       DEFAULT FALSE,
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_student_id UNIQUE (student_id),
    CONSTRAINT uk_email UNIQUE (email)
);
CREATE INDEX idx_users_campus ON users (campus);
CREATE INDEX idx_users_status ON users (status);

-- 2. 邮箱验证码表
CREATE TABLE email_verifications (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    email       VARCHAR(100) NOT NULL,
    code        VARCHAR(10)  NOT NULL,
    type        VARCHAR(20)  NOT NULL,
    expires_at  TIMESTAMP    NOT NULL,
    used        BOOLEAN      DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE INDEX idx_ev_email_type ON email_verifications (email, type);

-- 2.1 邮箱验证令牌表
CREATE TABLE email_tokens (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    token       VARCHAR(128) NOT NULL,
    type        VARCHAR(20)  NOT NULL,
    expires_at  TIMESTAMP    NOT NULL,
    used        BOOLEAN      DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_et_token UNIQUE (token)
);
CREATE INDEX idx_et_user_type ON email_tokens (user_id, type);

-- 3. Refresh Token 表
CREATE TABLE refresh_tokens (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    token       VARCHAR(500) NOT NULL,
    expires_at  TIMESTAMP    NOT NULL,
    revoked     BOOLEAN      DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_rt_token UNIQUE (token)
);
CREATE INDEX idx_rt_user ON refresh_tokens (user_id);

-- 4. 商品表
CREATE TABLE products (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    user_id         BIGINT        NOT NULL,
    title           VARCHAR(60)   NOT NULL,
    description     CLOB          NOT NULL,
    price           BIGINT        NOT NULL,
    original_price  BIGINT        DEFAULT NULL,
    category        VARCHAR(20)   NOT NULL,
    condition       VARCHAR(20)   NOT NULL,
    campus          VARCHAR(30)   NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'pending_review',
    view_count      INT           DEFAULT 0,
    favorite_count  INT           DEFAULT 0,
    version         INT           DEFAULT 0,
    deleted         BOOLEAN       DEFAULT FALSE,
    audit_reason    VARCHAR(500)  DEFAULT NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE INDEX idx_p_user_status ON products (user_id, status);
CREATE INDEX idx_p_status_time ON products (status, created_at);
CREATE INDEX idx_p_campus_cat ON products (campus, category);
CREATE INDEX idx_p_price ON products (price);

-- 5. 商品图片表
CREATE TABLE product_images (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    product_id  BIGINT       NOT NULL,
    url         VARCHAR(500) NOT NULL,
    sort_order  INT          DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE INDEX idx_pi_product ON product_images (product_id);

-- 6. 收藏表
CREATE TABLE favorites (
    id          BIGINT    NOT NULL AUTO_INCREMENT,
    user_id     BIGINT    NOT NULL,
    product_id  BIGINT    NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_fav_user_product UNIQUE (user_id, product_id)
);
CREATE INDEX idx_fav_user ON favorites (user_id);

-- 7. 订单表
CREATE TABLE orders (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    order_no            VARCHAR(32)  NOT NULL,
    buyer_id            BIGINT       NOT NULL,
    seller_id           BIGINT       NOT NULL,
    product_id          BIGINT       NOT NULL,
    amount              BIGINT       NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'pending_payment',
    payment_method      VARCHAR(20)  DEFAULT NULL,
    transaction_id      VARCHAR(64)  DEFAULT NULL,
    refund_id           VARCHAR(64)  DEFAULT NULL,
    refund_amount       BIGINT       DEFAULT NULL,
    refund_reason       VARCHAR(500) DEFAULT NULL,
    refund_requested_at TIMESTAMP    DEFAULT NULL,
    refund_completed_at TIMESTAMP    DEFAULT NULL,
    paid_at             TIMESTAMP    DEFAULT NULL,
    completed_at        TIMESTAMP    DEFAULT NULL,
    cancelled_at        TIMESTAMP    DEFAULT NULL,
    expires_at          TIMESTAMP    NOT NULL,
    version             INT          DEFAULT 0,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_order_no UNIQUE (order_no)
);
CREATE INDEX idx_o_buyer_status ON orders (buyer_id, status);
CREATE INDEX idx_o_seller_status ON orders (seller_id, status);
CREATE INDEX idx_o_product ON orders (product_id);

-- 8. 会话表
CREATE TABLE conversations (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    product_id      BIGINT       NOT NULL,
    buyer_id        BIGINT       NOT NULL,
    seller_id       BIGINT       NOT NULL,
    last_message    VARCHAR(500) DEFAULT NULL,
    last_message_at TIMESTAMP    DEFAULT NULL,
    buyer_unread    INT          DEFAULT 0,
    seller_unread   INT          DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_conv_product_buyer UNIQUE (product_id, buyer_id)
);
CREATE INDEX idx_conv_buyer ON conversations (buyer_id);
CREATE INDEX idx_conv_seller ON conversations (seller_id);

-- 9. 消息表
CREATE TABLE messages (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    conversation_id BIGINT        NOT NULL,
    sender_id       BIGINT        NOT NULL,
    receiver_id     BIGINT        NOT NULL,
    content         VARCHAR(1000) NOT NULL,
    type            VARCHAR(20)   NOT NULL DEFAULT 'text',
    is_read         BOOLEAN       DEFAULT FALSE,
    read_at         TIMESTAMP     DEFAULT NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE INDEX idx_msg_conv_time ON messages (conversation_id, created_at);
CREATE INDEX idx_msg_receiver_read ON messages (receiver_id, is_read);

-- 10. 评价表
CREATE TABLE reviews (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    order_id    BIGINT       NOT NULL,
    reviewer_id BIGINT       NOT NULL,
    target_id   BIGINT       NOT NULL,
    rating      TINYINT      NOT NULL,
    content     VARCHAR(500) DEFAULT NULL,
    tags        VARCHAR(200) DEFAULT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_review_order UNIQUE (order_id, reviewer_id)
);
CREATE INDEX idx_rev_target ON reviews (target_id);

-- 11. 纠纷/工单表
CREATE TABLE disputes (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    order_id        BIGINT       NOT NULL,
    reporter_id     BIGINT       NOT NULL,
    type            VARCHAR(20)  NOT NULL,
    reason          VARCHAR(500) NOT NULL,
    evidence_urls   CLOB         DEFAULT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'pending',
    resolution      VARCHAR(500) DEFAULT NULL,
    resolved_by     BIGINT       DEFAULT NULL,
    resolved_at     TIMESTAMP    DEFAULT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE INDEX idx_disp_order ON disputes (order_id);
CREATE INDEX idx_disp_status ON disputes (status);

-- 12. 通知表
CREATE TABLE notifications (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    type        VARCHAR(30)  NOT NULL,
    title       VARCHAR(100) NOT NULL,
    content     VARCHAR(500) NOT NULL,
    target_type VARCHAR(20)  DEFAULT NULL,
    target_id   BIGINT       DEFAULT NULL,
    is_read     BOOLEAN      DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE INDEX idx_notif_user_read ON notifications (user_id, is_read, created_at);

-- 13. 敏感词表
CREATE TABLE sensitive_words (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    word       VARCHAR(50) NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_sw_word UNIQUE (word)
);

-- 14. 纠纷评论表
CREATE TABLE dispute_comments (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    dispute_id  BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    content     VARCHAR(500) NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE INDEX idx_dc_dispute ON dispute_comments (dispute_id);

-- 15. 登录历史表
CREATE TABLE login_history (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    login_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip          VARCHAR(50)  DEFAULT NULL,
    device      VARCHAR(200) DEFAULT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX idx_lh_user_login ON login_history (user_id, login_at);

-- ============================================================
-- 种子数据：插入测试用户和商品
-- ============================================================

-- 测试用户 (密码 BCrypt: Pass1234)
INSERT INTO users (id, student_id, nickname, password_hash, email, email_verified, campus, role, status, reputation_score)
VALUES (1, '2024010101', '张三同学', '$2a$10$C9Z0/MOdzbhS5PhQ3csecOTzlrPEnqBSm2YINO90HKj6YQASxwEaS', 'zhangsan@campus.edu', TRUE, '本部校区', 'user', 'active', 4.8);

INSERT INTO users (id, student_id, nickname, password_hash, email, email_verified, campus, role, status, reputation_score)
VALUES (2, '2024010102', '李四同学', '$2a$10$C9Z0/MOdzbhS5PhQ3csecOTzlrPEnqBSm2YINO90HKj6YQASxwEaS', 'lisi@campus.edu', TRUE, '本部校区', 'user', 'active', 4.5);

INSERT INTO users (id, student_id, nickname, password_hash, email, email_verified, campus, role, status, reputation_score)
VALUES (3, '2024010103', '王五同学', '$2a$10$C9Z0/MOdzbhS5PhQ3csecOTzlrPEnqBSm2YINO90HKj6YQASxwEaS', 'wangwu@campus.edu', TRUE, '东校区', 'user', 'active', 4.2);

-- 测试商品
INSERT INTO products (id, user_id, title, description, price, category, condition, campus, status, created_at, updated_at)
VALUES (1, 2, '微积分（上册）', '九成新，只有前两章有少量笔记，其余全新。高等教育出版社。', 2500, 'textbook', 'like_new', '本部校区', 'on_sale', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO products (id, user_id, title, description, price, category, condition, campus, status, created_at, updated_at)
VALUES (2, 2, '大学英语四级词汇书', '全新未使用，买多了出一本。', 1500, 'textbook', 'brand_new', '本部校区', 'on_sale', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO products (id, user_id, title, description, price, category, condition, campus, status, created_at, updated_at)
VALUES (3, 3, 'iPad Air 5 64G', '去年买的，一直带壳贴膜使用，无划痕无维修。配件齐全。', 320000, 'digital', 'like_new', '东校区', 'on_sale', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO products (id, user_id, title, description, price, category, condition, campus, status, created_at, updated_at)
VALUES (4, 3, '台灯 LED 护眼', '宿舍用不到了，可调节亮度和色温。', 3500, 'lifestyle', 'slightly_used', '东校区', 'on_sale', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO products (id, user_id, title, description, price, category, condition, campus, status, created_at, updated_at)
VALUES (5, 1, '篮球 Spalding', '只用了一个学期，手感很好。', 6000, 'sports', 'slightly_used', '本部校区', 'on_sale', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 测试商品图片
INSERT INTO product_images (id, product_id, url, sort_order) VALUES (1, 1, 'https://picsum.photos/seed/p1/400/400', 0);
INSERT INTO product_images (id, product_id, url, sort_order) VALUES (2, 2, 'https://picsum.photos/seed/p2/400/400', 0);
INSERT INTO product_images (id, product_id, url, sort_order) VALUES (3, 3, 'https://picsum.photos/seed/p3/400/400', 0);
INSERT INTO product_images (id, product_id, url, sort_order) VALUES (4, 4, 'https://picsum.photos/seed/p4/400/400', 0);
INSERT INTO product_images (id, product_id, url, sort_order) VALUES (5, 5, 'https://picsum.photos/seed/p5/400/400', 0);