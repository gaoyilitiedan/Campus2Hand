# Campus2Hand — API 接口设计

---

## 文档信息

| 项目       | 说明                          |
|------------|-------------------------------|
| 文档名称   | API 接口设计                  |
| 版本       | v1.0                          |
| 日期       | 2026-05-13                    |
| 基础路径   | `/api/v1`                     |
| 协议       | HTTPS (TLS 1.2+)              |
| 数据格式   | JSON                          |
| 认证方式   | Bearer Token (JWT HS256)      |
| 框架       | Spring Boot 3.2.x + Spring MVC |

---

## 目录

1. [通用规范](#1-通用规范)
2. [认证模块 API](#2-认证模块-api)
3. [用户模块 API](#3-用户模块-api)
4. [商品模块 API](#4-商品模块-api)
5. [订单模块 API](#5-订单模块-api)
6. [聊天模块 API](#6-聊天模块-api)
7. [评价模块 API](#7-评价模块-api)
8. [投诉工单模块 API](#8-投诉工单模块-api)
9. [微信支付回调](#9-微信支付回调)
10. [WebSocket 协议](#10-websocket-协议)

---

## 1. 通用规范

### 1.1 统一响应格式 `ApiResponse<T>`

```java
public class ApiResponse<T> {
    private int    code;       // 业务状态码，0 = 成功
    private String message;    // 提示信息
    private T      data;       // 响应数据
    private String timestamp;  // ISO 8601 UTC
    private String traceId;    // 链路追踪ID
}
```

**示例：**

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "timestamp": "2026-05-13T08:00:00Z",
  "traceId": "a1b2c3d4-e5f6-7890"
}
```

### 1.2 分页响应 `PageResponse<T>`

```java
public class PageResponse<T> {
    private int    page;     // 当前页码
    private int    size;     // 每页数量
    private long   total;    // 总记录数
    private int    pages;    // 总页数
    private List<T> list;    // 数据列表
}
```

### 1.3 通用错误码枚举

```java
public enum ErrorCode {
    SUCCESS(0,            "成功"),
    PARAM_INVALID(1001,   "参数校验失败"),
    RESOURCE_NOT_FOUND(1002, "资源不存在"),
    NO_PERMISSION(1003,   "无权限"),
    TOKEN_INVALID(1004,   "Token 过期或无效"),
    RATE_LIMITED(1005,    "请求过于频繁"),
    USER_NOT_VERIFIED(2001, "用户未实名认证"),
    ACCOUNT_LOCKED(2002,  "账户已锁定"),
    PAY_FAILED(3001,      "支付失败"),
    ORDER_STATUS_ERROR(3002, "订单状态不合法"),
    INTERNAL_ERROR(5000,  "服务器内部错误");

    private final int code;
    private final String message;
}
```

### 1.4 Controller 层代码规范

```java
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success("注册成功，请前往校园邮箱完成邮箱验证");
    }

    // ... 其他接口
}
```

**约定：**
- 所有 DTO 使用 `@Valid` 注解触发 Bean Validation
- Service 层返回业务对象，Controller 负责包装 `ApiResponse`
- 异常统一由 `GlobalExceptionHandler` 处理，禁止 Controller 内 try-catch
- 金额字段在 API 中统一使用 `int` 类型，单位为**分**

### 1.5 接口安全分类

| 安全级别          | 说明              | 实现方式                               |
|-------------------|-------------------|----------------------------------------|
| ⚪ 公开接口       | 无需登录          | 注册、登录、忘记密码                    |
| 🔵 需登录         | 仅需有效 JWT      | `@PreAuthorize("isAuthenticated()")`   |
| 🟢 需实名认证     | 登录 + 已认证     | 发布商品、聊天、下单                    |
| 🟡 所有者权限     | 仅资源所有者      | 编辑/下架自己的商品、处理自己的订单     |
| 🔴 管理员权限     | 后台管理          | 工单受理、内容审核                      |

---

## 2. 认证模块 API

> **Controller:** `com.campus2hand.auth.controller.AuthController`
> **基础路径:** `/api/v1/auth`

### 2.1 注册

```
POST /api/v1/auth/register     ⚪ 公开
```

| 字段             | 类型   | 必填 | 校验规则                              |
|------------------|--------|------|---------------------------------------|
| studentId        | string | 是   | @Pattern(regexp = "^\\d{8,12}$")      |
| campusPassword   | string | 是   | @NotBlank                             |
| platformPassword | string | 是   | @Size(min=8) + 大小写字母+数字正则    |
| campusEmail      | string | 是   | @Email + 域名后缀校验                 |
| phone            | string | 否   | @Pattern(regexp = "^1[3-9]\\d{9}$")   |

```json
{
  "studentId": "2024010101",
  "campusPassword": "******",
  "platformPassword": "Pass1234",
  "campusEmail": "zhangsan@campus.edu"
}
```

**处理流程：**
1. Controller 接收 → `@Valid` 校验
2. `AuthService.register()`:
   - 检查 `student_id` 唯一
   - 调用校园系统 HTTP 接口校验校园卡密码（超时 5s，熔断 10 次/60s）
   - `password_hash = BCrypt.hashpw(platformPassword, BCrypt.gensalt(10))`
   - 插入 `users` 表（`is_verified=0`）
   - 生成 24h 有效期 email token → 插入 `email_tokens`
   - `@Async` 发送验证邮件

> **联动边界条件 BC-A01~A05、BC-A10**：[spec.md#边界条件](file:///c:/Users/27355/Desktop/Campus2Hand/spec.md)

### 2.2 登录

```
POST /api/v1/auth/login        ⚪ 公开
```

| 字段      | 类型   | 校验                  |
|-----------|--------|-----------------------|
| studentId | string | @NotBlank             |
| password  | string | @NotBlank             |

**处理流程：**
1. 查询 `users` by `student_id`
2. 检查 `is_locked` → 若未过期返回 2002 "已锁定"
3. `BCrypt.checkpw(password, user.password_hash)`
4. 失败 → `login_fail_count++`，≥ 5 次 → 锁定 30 分钟
5. 成功 → 生成 Access Token（2h HS256）+ Refresh Token（7d HS256）返回

```json
{
  "code": 0,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresAt": "2026-05-13T10:00:00Z",
    "user": {
      "id": 1,
      "studentId": "20****01",
      "nickname": "张三",
      "campus": "本部校区",
      "reputationScore": 4.8,
      "isVerified": true
    }
  }
}
```

### 2.3 发送邮箱验证链接

```
POST /api/v1/auth/send-verify-email    🔵 需登录
```

| 字段      | 类型   | 校验      |
|-----------|--------|-----------|
| studentId | string | @NotBlank |

**边界条件 BC-A03/A04/A05**：链接 24h 有效 / 单日最多 5 次 / 邮箱重复绑定拒绝。

### 2.4 确认邮箱验证

```
POST /api/v1/auth/verify-email         ⚪ 公开
```

| 字段  | 类型   |
|-------|--------|
| token | string |

校验 `email_tokens` 表中 token 有效性，未过期 + 未使用 → 更新 `users.is_verified=1`。

### 2.5 忘记密码

```
POST /api/v1/auth/forgot-password      ⚪ 公开
```

| 字段      | 类型   |
|-----------|--------|
| studentId | string |

向校园邮箱发送 30min 有效重置链接。

### 2.6 重置密码

```
POST /api/v1/auth/reset-password       ⚪ 公开
```

| 字段        | 类型   |
|-------------|--------|
| token       | string |
| newPassword | string |

校验 token → 重置密码 → **所有已有 JWT 加入 Redis 黑名单 + tokenVersion++**（强制登出）。

### 2.7 Refresh Token 刷新

```
POST /api/v1/auth/refresh              ⚪ 公开
```

| 字段         | 类型   | 说明      |
|--------------|--------|-----------|
| refreshToken | string | Refresh Token |

**处理流程：** 校验 Refresh Token 签名 + 有效期 → 签发新 Access Token（2h） + 新 Refresh Token（7d），旧 Refresh Token 加入黑名单（滚动刷新）。

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "eyJhbGciOi...",
    "expiresAt": "2026-05-13T12:00:00Z"
  }
}
```

### 2.8 修改密码（已知旧密码）

```
PUT /api/v1/auth/change-password        🔵 需登录
```

| 字段        | 类型   | 说明    |
|-------------|--------|---------|
| oldPassword | string | 旧密码  |
| newPassword | string | 新密码  |

校验旧密码正确 → 更新 `password_hash` → 所有设备 Token 强制登出。

---

## 3. 用户模块 API

> **Controller:** `com.campus2hand.user.controller.UserController`
> **基础路径:** `/api/v1/users`

### 3.1 获取当前用户信息

```
GET /api/v1/users/me                   🔵 需登录
```

返回脱敏后的用户信息（`maskStudentId`、`maskEmail`、`maskPhone`）。

### 3.2 更新用户信息

```
PUT /api/v1/users/me                   🔵 需登录
```

| 字段      | 类型       | 校验                          |
|-----------|------------|-------------------------------|
| nickname  | string     | @Size(min=1, max=20)          |
| avatar    | multipart  | ≤ 2MB, JPG/PNG                |
| campus    | string     | 白名单校验                     |

### 3.3 绑定手机号

```
PUT /api/v1/users/me/phone              🔵 需登录
```

| 字段  | 类型   | 说明                          |
|-------|--------|-------------------------------|
| phone | string | 手机号，AES-256 加密存储       |

校验手机号格式 → AES-256 加密 → 更新 `users.phone`。

### 3.4 获取用户信誉分

```
GET /api/v1/users/{userId}/reputation  🔵 需登录
```

返回：信誉分、评分分布 `{"5": 45, "4": 8, ...}`、最近评价列表。

### 3.5 获取登录记录

```
GET /api/v1/users/me/login-history     🔵 需登录
```

返回最近 10 次登录记录。

---

## 4. 商品模块 API

> **Controller:** `com.campus2hand.product.controller.ProductController`
> **基础路径:** `/api/v1/products`

### 4.1 搜索商品

```
GET /api/v1/products/search            ⚪ 公开
```

**查询参数 (Query String)：**

| 字段      | 类型   | 必填 | 说明                                              |
|-----------|--------|------|---------------------------------------------------|
| keyword   | string | 否   | 关键词（MATCH FULLTEXT）                           |
| category  | string | 否   | textbook/digital/lifestyle/sports/clothing/other   |
| minPrice  | int    | 否   | 最低价格（分）                                      |
| maxPrice  | int    | 否   | 最高价格（分）                                      |
| condition | string | 否   | brand_new/like_new/slightly_used/visibly_used       |
| campus    | string | 否   | 校区                                              |
| sortBy    | string | 否   | created_at / price_asc / price_desc / reputation_desc |
| page      | int    | 否   | 默认 1                                            |
| size      | int    | 否   | 默认 20，最大 50                                   |

**Service 层查询逻辑：**

```java
LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Product::getStatus, "on_sale");

if (StringUtils.hasText(keyword)) {
    wrapper.apply("MATCH(title, description) AGAINST({0} IN BOOLEAN MODE)", keyword);
}
if (StringUtils.hasText(category)) {
    wrapper.eq(Product::getCategory, category);
}
// ... 其他条件

// 排序
switch (sortBy) {
    case "price_asc"  -> wrapper.orderByAsc(Product::getPrice);
    case "price_desc" -> wrapper.orderByDesc(Product::getPrice);
    case "reputation_desc" -> { /* 子查询信誉分排序 */ }
    default -> wrapper.orderByDesc(Product::getCreatedAt);
}

Page<Product> page = new Page<>(pageNum, pageSize);
productMapper.selectPage(page, wrapper);
```

**响应：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "page": 1, "size": 20, "total": 87, "pages": 5,
    "list": [
      {
        "id": 1,
        "title": "微积分（上册）",
        "coverImage": "https://oss.example.com/products/1/cover_thumb.jpg",
        "price": 2500,
        "condition": "like_new",
        "category": "textbook",
        "campus": "本部校区",
        "seller": {
          "id": 2, "nickname": "李四同学",
          "reputationScore": 4.5
        },
        "createdAt": "2026-05-12T14:30:00Z"
      }
    ]
  }
}
```

### 4.2 获取商品详情

```
GET /api/v1/products/{productId}        ⚪ 公开
```

返回完整商品信息 + 脱敏卖家信息（含 `totalSold`、`reviewCount`）。

### 4.3 获取卖家评价列表

```
GET /api/v1/products/{productId}/seller-reviews    ⚪ 公开
```

分页返回该商品卖家的历史评价：

```java
reviewMapper.selectPage(page,
    new LambdaQueryWrapper<Review>()
        .eq(Review::getRevieweeId, sellerId)
        .eq(Review::getIsPublic, true)
        .orderByDesc(Review::getCreatedAt)
);
```

### 4.4 发布商品

```
POST /api/v1/products                  🟢 需实名认证
Content-Type: multipart/form-data
```

| 字段        | 类型         | 必填 | 说明                    |
|-------------|--------------|------|-------------------------|
| images      | file[]       | 是   | 1-9 张，JPG/PNG/WebP，单张 ≤ 10MB |
| title       | string       | 是   | 1-60 字符               |
| description | string       | 是   | 1-2000 字符             |
| price       | int          | 是   | 1-9999999（分）          |
| category    | string       | 是   |                         |
| condition   | string       | 是   |                         |
| campus      | string       | 是   | 白名单校验               |

**处理流程：**

```
① 图片校检 (MIME + 文件头魔数)
② 压缩 → OSS 上传 → 返回 URL[]
③ 字段校验 + 校区白名单
④ 敏感词同步检测 (sensitive_words 表)
⑤ 插入 products (status='pending_review')
⑥ @Async 图片内容审核 (异步)
⑦ 返回 ApiResponse + 审核状态
```

### 4.5 更新商品

```
PUT /api/v1/products/{productId}       🟡 所有者权限
Content-Type: multipart/form-data
```

仅 `on_sale` / `pending_review` 状态可编辑，修改后 `status` 重置为 `pending_review`。

### 4.6 下架 / 标记售出 / 我的商品列表

| 方法 | 路径                                   | 权限     | 说明                  |
|------|----------------------------------------|----------|-----------------------|
| POST | `/api/v1/products/{productId}/delist`  | 🟡 所有者 | 从公开列表移除          |
| POST | `/api/v1/products/{productId}/mark-sold`| 🟡 所有者 | 手动标记已售出          |
| GET  | `/api/v1/my/products`                  | 🔵 需登录 | 分状态 Tab 查询（分页）  |

**商品管理操作需使用乐观锁：**

```java
UpdateWrapper<Product> wrapper = new UpdateWrapper<>();
wrapper.eq("id", productId)
       .eq("version", currentVersion)   // 乐观锁
       .set("status", "delisted")
       .setSql("version = version + 1");
```

---

## 5. 订单模块 API

> **Controller:** `com.campus2hand.order.controller.OrderController`
> **基础路径:** `/api/v1/orders`

### 5.1 创建订单

```
POST /api/v1/orders                     🟢 需实名认证
```

| 字段      | 类型   | 说明   |
|-----------|--------|--------|
| productId | long   | 商品ID |

**处理流程（含并发控制）：**

```java
@Transactional
public Order createOrder(Long userId, Long productId) {
    // 1. Redis 分布式锁
    RLock lock = redissonClient.getLock("lock:order:" + productId);
    try {
        lock.lock(10, TimeUnit.SECONDS);
        // 2. 查询商品，校验 status='on_sale' + sellerId != userId
        Product p = productMapper.selectById(productId);
        if (p == null || !"on_sale".equals(p.getStatus())) {
            throw new BusinessException("商品已下架或已售出");
        }
        // 3. 创建订单（status=pending_payment, expire_at=now+30min）
        // 4. 创建支付记录（status=pending）
        // 5. 发送通知给卖家
        return order;
    } finally {
        lock.unlock();
    }
}
```

> **联动边界条件 BC-T01、BC-T06**：[spec.md#边界条件](file:///c:/Users/27355/Desktop/Campus2Hand/spec.md)

### 5.2 发起支付

```
POST /api/v1/orders/{orderId}/pay       🟡 买家权限
```

调用微信支付 JSAPI 统一下单 → 返回 `prepay_id` + 签名参数供前端唤起微信支付。

### 5.3 确认收货

```
POST /api/v1/orders/{orderId}/confirm-receipt   🟡 买家权限
```

**处理流程：**

```java
@Transactional
public void confirmReceipt(Long orderId) {
    Order order = orderMapper.selectById(orderId);
    if (!"paid_escrow".equals(order.getStatus()) && !"pending_shipment".equals(order.getStatus())) {
        throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
    }
    // ① 更新订单 status=completed
    // ② @Async 调用微信商家转账到零钱 → 结算给卖家
    // ③ 卖家 total_sold++, 买家 total_bought++
    // ④ 结算成功 → status=completed；结算失败 → status=settlement_failed + 重试队列
    // ⑤ 发送通知
}
```

### 5.4 申请退款

```
POST /api/v1/orders/{orderId}/refund    🟡 买家权限
```

| 字段         | 类型     | 必填 | 说明                         |
|--------------|----------|------|------------------------------|
| reason       | string   | 是   | not_as_described/seller_unreachable/product_damaged/other |
| description  | string   | 是   | 1-500 字符                   |
| images       | file[]   | 否   | 最多 3 张，单张 ≤ 5MB        |

订单状态 → `refunding`，卖家 48h 内处理。

### 5.5 卖家处理退款

```
POST /api/v1/orders/{orderId}/refund/handle   🟡 卖家权限
```

| 字段    | 类型   | 说明              |
|---------|--------|-------------------|
| action  | string | agree / reject    |
| comment | string | 处理说明（拒绝必填）|

- `agree` → 微信退款原路退回 → `status=refunded`
- `reject` → `status=paid_escrow`（恢复）

### 5.6 查询接口

| 方法 | 路径                       | 说明                     |
|------|----------------------------|--------------------------|
| GET  | `/api/v1/orders/{orderId}` | 订单详情（含状态时间线）   |
| GET  | `/api/v1/my/orders`        | 我的订单（按角色+状态筛选）|

---

## 6. 聊天模块 API

> **Controller:** `com.campus2hand.chat.controller.ChatController`
> **基础路径:** `/api/v1`

### 6.1 获取会话列表

```
GET /api/v1/conversations               🔵 需登录
```

**查询逻辑：**

```java
// 查询当前用户作为 buyer 或 seller 的所有会话
List<Conversation> conversations = conversationMapper.selectList(
    new LambdaQueryWrapper<Conversation>()
        .and(w -> w.eq(Conversation::getBuyerId, userId)
                   .or()
                   .eq(Conversation::getSellerId, userId))
        .orderByDesc(Conversation::getUpdatedAt)
);
// 关联查询 participant_reads 表获取未读数
```

**响应字段：** 对方昵称、关联商品标题、最后消息摘要、时间、未读角标数量。

### 6.2 获取历史消息

```
GET /api/v1/conversations/{conversationId}/messages   🔵 需登录
```

**查询参数：**

| 字段   | 类型   | 说明                               |
|--------|--------|------------------------------------|
| before | long   | 消息ID游标，获取此ID之前的消息      |
| size   | int    | 默认 30，最大 100                   |

```java
LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Message::getConversationId, convId)
       .lt(before != null, Message::getId, before)  // 游标分页
       .orderByDesc(Message::getId)
       .last("LIMIT " + size);
```

### 6.3 上传聊天图片

```
POST /api/v1/conversations/{conversationId}/upload-image   🔵 需登录
Content-Type: multipart/form-data
```

图片压缩至 ≤ 500KB → 上传 OSS → 返回 URL（前端拿到 URL 后通过 WebSocket 发送图片消息）。

### 6.4 标记已读

```
POST /api/v1/conversations/{conversationId}/read   🔵 需登录
```

| 字段          | 类型 | 说明                |
|---------------|------|---------------------|
| lastReadMsgId | long | 最后已读消息ID       |

更新 `participant_reads` 表：`last_read_msg_id` += 1，`unread_count = 0`。

---

## 7. 评价模块 API

> **Controller:** `com.campus2hand.review.controller.ReviewController`
> **基础路径:** `/api/v1`

### 7.1 提交评价

```
POST /api/v1/reviews                     🔵 需登录
```

| 字段    | 类型     | 必填 | 校验               |
|---------|----------|------|--------------------|
| orderId | long     | 是   | 订单已完成          |
| rating  | int      | 是   | @Min(1) @Max(5)    |
| content | string   | 否   | @Size(max=500)      |
| tags    | string[] | 否   | @Size(max=3)        |

**信誉分计算逻辑（Service 层）：**

```java
@Transactional
public void submitReview(Long userId, SubmitReviewRequest req) {
    // 1. 防重复：uk_order_reviewer 唯一约束
    // 2. 插入 review (is_public = 对方也已评价)
    // 3. 如果双方均已评价 → 两条都更新 is_public=true
    // 4. 重新计算 reviewee 的信誉分：
    //    SELECT AVG(rating) FROM reviews WHERE reviewee_id = ? AND is_public = true
    //    保留 1 位小数，使用 BigDecimal.ROUND_HALF_EVEN
    // 5. 更新 users.reputation_score 和 users.review_count
}
```

### 7.2 获取用户信誉分

```
GET /api/v1/users/{userId}/reputation    🔵 需登录
```

（同 3.3）

### 7.3 定时公开评价

单方评价 7 天后自动公开 —— `@Scheduled(cron = "0 0 2 * * ?")` 每天凌晨 2 点扫描 `reviews` 表中 `is_public=0` 且 `created_at < now - 7d` 的记录。

---

## 8. 投诉工单模块 API

> **Controller:** `com.campus2hand.dispute.controller.DisputeController`
> **基础路径:** `/api/v1/disputes`

### 8.1 创建工单

```
POST /api/v1/disputes                    🟢 需实名认证
```

| 字段        | 类型     | 必填 | 说明                              |
|-------------|----------|------|-----------------------------------|
| orderId     | long     | 是   | 关联订单                           |
| type        | string   | 是   | product_issue/payment_issue/behavior_violation/other |
| description | string   | 是   | 1-2000 字符                        |
| images      | file[]   | 否   | 最多 5 张                          |

校验：`order_id` 存在 + 无进行中的工单 → 插入 `status=pending`。

### 8.2 查询接口

| 方法 | 路径                           | 说明          |
|------|--------------------------------|---------------|
| GET  | `/api/v1/disputes/{disputeId}` | 工单详情+留言  |
| GET  | `/api/v1/my/disputes`          | 我的工单列表   |

### 8.3 追加留言

```
POST /api/v1/disputes/{disputeId}/comments    🔵 需登录
```

| 字段    | 类型   | 说明       |
|---------|--------|------------|
| content | string | 1-1000 字符 |

双方均可留言（仅 `pending` / `processing` 状态可追加）。

### 8.4 管理员处理接口

```
PUT /api/v1/admin/disputes/{disputeId}/status    🔴 管理员权限
```

| 字段   | 类型   | 说明                                   |
|--------|--------|----------------------------------------|
| status | string | processing / resolved / rejected       |
| comment| string | 处理说明（resolved/rejected 时必填）    |

---

## 9. 通知模块 API

> **Controller:** `com.campus2hand.notification.controller.NotificationController`
> **基础路径:** `/api/v1/notifications`

### 9.1 获取通知列表

```
GET /api/v1/notifications                🔵 需登录
```

| 字段   | 类型   | 说明                             |
|--------|--------|----------------------------------|
| page   | int    | 默认 1，每页 20                   |
| unread | bool   | 仅未读（可选）                    |

**响应：** 分页返回通知列表，含 `type`、`title`、`content`、`isRead`、`createdAt`。

### 9.2 标记已读

```
POST /api/v1/notifications/{id}/read     🔵 需登录
```

### 9.3 全部已读

```
POST /api/v1/notifications/read-all      🔵 需登录
```

### 9.4 获取未读计数

```
GET /api/v1/notifications/unread-count   🔵 需登录
```

> 通知通过 Spring 事件机制异步生成：订单状态变更、退款处理、新消息等事件 → `@EventListener` → 插入 `notifications` 表。

---

## 10. 管理员后台 API

> **Controller:** `com.campus2hand.admin.controller.AdminController`
> **基础路径:** `/api/v1/admin`

### 10.1 用户管理

| 方法 | 路径                                   | 说明               |
|------|----------------------------------------|--------------------|
| GET  | `/api/v1/admin/users`                  | 搜索/分页用户列表   |
| PUT  | `/api/v1/admin/users/{id}/lock`        | 封禁/解封用户       |
| GET  | `/api/v1/admin/users/{id}/orders`      | 查看用户交易记录     |

### 10.2 商品审核

```
GET /api/v1/admin/products/pending       🔴 管理员
```

返回 `status=pending_review` 的商品。

```
PUT /api/v1/admin/products/{id}/audit    🔴 管理员
```

| 字段   | 类型   | 说明                           |
|--------|--------|--------------------------------|
| action | string | approve / reject               |
| reason | string | 驳回原因（reject 时必填）       |

### 10.3 工单管理

```
PUT /api/v1/admin/disputes/{id}/status   🔴 管理员
```

| 字段    | 类型   | 说明                               |
|---------|--------|------------------------------------|
| status  | string | processing / resolved / rejected   |
| comment | string | 处理说明                           |

### 10.4 敏感词维护

| 方法   | 路径                                | 说明           |
|--------|-------------------------------------|----------------|
| GET    | `/api/v1/admin/sensitive-words`     | 列表+搜索      |
| POST   | `/api/v1/admin/sensitive-words`     | 新增           |
| DELETE | `/api/v1/admin/sensitive-words/{id}`| 删除           |

### 10.5 数据统计

```
GET /api/v1/admin/statistics            🔴 管理员
```

返回：日活用户、新增商品、成交量、纠纷率等核心指标。

---

## 11. 微信支付回调

```
POST /api/v1/callback/wechat-pay        ⚪ 公开（签名认证）
```

> **Controller:** `com.campus2hand.order.controller.PayCallbackController`

**处理流程：**

```java
@PostMapping("/api/v1/callback/wechat-pay")
public Map<String, String> wechatPayCallback(
    @RequestHeader("Wechatpay-Signature") String signature,
    @RequestHeader("Wechatpay-Nonce") String nonce,
    @RequestHeader("Wechatpay-Timestamp") String timestamp,
    @RequestHeader("Wechatpay-Serial") String serial,
    @RequestBody String body
) {
    // ① 验签（微信平台公钥 + SHA256-RSA2048）
    if (!wechatPayService.verifySignature(signature, nonce, timestamp, body)) {
        log.error("微信支付回调验签失败");
        return Map.of("code", "FAIL", "message", "签名验证失败");
    }
    // ② 解密回调报文（AEAD_AES_256_GCM）
    CallbackData data = wechatPayService.decryptCallback(body);
    // ③ 幂等处理（order_no 去重）
    Payment payment = paymentMapper.selectOne(
        new LambdaQueryWrapper<Payment>()
            .eq(Payment::getOrderNo, data.getOutTradeNo())
    );
    if (payment != null && "success".equals(payment.getStatus())) {
        return Map.of("code", "SUCCESS");  // 已处理
    }
    // ④ 校验金额一致性
    Order order = orderMapper.selectOne(
        new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, data.getOutTradeNo())
    );
    if (order.getAmount() != data.getAmount().getTotal()) {
        log.error("支付金额不一致 order={} callback={}", order.getAmount(), data.getAmount());
        // 告警 → 人工处理
        return Map.of("code", "FAIL", "message", "金额校验失败");
    }
    // ⑤ 更新订单 status=paid_escrow + 支付记录 status=success
    paymentService.handleSuccess(order, data);
    return Map.of("code", "SUCCESS");
}
```

> **联动边界条件 BC-T02~T05**：[spec.md#边界条件](file:///c:/Users/27355/Desktop/Campus2Hand/spec.md)

---

## 12. WebSocket 协议

### 10.1 连接端点

```
wss://api.campus2hand.com/ws/chat
```

**连接流程：**

```
客户端 → WebSocket 连接（URL 带 ?token=<JWT>）
服务端 → ChannelInterceptor 拦截:
   ① 从 URL 提取 JWT
   ② 校验 JWT 有效性
   ③ 提取 userId 存入 WebSocket Session attributes
   ④ 建立连接
```

**Spring WebSocket 配置：**

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
                .setHandshakeHandler(new JwtHandshakeHandler())
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new JwtChannelInterceptor());
    }
}
```

### 10.2 消息帧格式

#### 10.2.1 发送消息（客户端 → 服务端）

```json
{
  "type": "message",
  "data": {
    "conversationId": 1,
    "messageType": "text",
    "content": "你好，这本书还在吗？",
    "clientMsgId": "c_uuid_001"
  }
}
```

**服务端处理：**
1. 校验 `clientMsgId` 唯一（查 `uk_client_msg_id`）
2. 校验限流（Redis 滑动窗口 5条/秒）
3. `messageType=text` → 内容长度 ≤ 500
4. 插入 `messages` 表
5. 更新 `conversations.last_message` + `updated_at`
6. 接收者在线 → STOMP `convertAndSendToUser(receiverId, "/queue/messages", msg)`
7. 接收者离线 → `participant_reads.unread_count++`
8. 返回 ACK 给发送者

#### 10.2.2 接收消息（服务端 → 客户端）

```json
{
  "type": "message",
  "data": {
    "id": 1001,
    "conversationId": 1,
    "senderId": 2,
    "messageType": "text",
    "content": "你好，这本书还在吗？",
    "clientMsgId": "c_uuid_001",
    "createdAt": "2026-05-13T08:00:00Z"
  }
}
```

#### 10.2.3 送达确认（服务端 → 发送者）

```json
{
  "type": "ack",
  "data": {
    "clientMsgId": "c_uuid_001",
    "serverMsgId": 1001,
    "status": "delivered"
  }
}
```

### 10.3 心跳保活

- 客户端每 **30s** 发送 STOMP `PING` 帧
- 服务端响应 `PONG`
- 连续 **3 次**（90s）无心跳 → 服务端断开连接，标记用户离线

---

## 附录 A：接口速查表

| 模块   | 方法   | 路径                                        | 权限    |
|--------|--------|---------------------------------------------|---------|
| 认证   | POST   | `/api/v1/auth/register`                     | ⚪      |
| 认证   | POST   | `/api/v1/auth/login`                        | ⚪      |
| 认证   | POST   | `/api/v1/auth/send-verify-email`            | 🔵      |
| 认证   | POST   | `/api/v1/auth/verify-email`                 | ⚪      |
| 认证   | POST   | `/api/v1/auth/forgot-password`              | ⚪      |
| 认证   | POST   | `/api/v1/auth/reset-password`               | ⚪      |
| 认证   | POST   | `/api/v1/auth/refresh`                      | ⚪      |
| 认证   | PUT    | `/api/v1/auth/change-password`              | 🔵      |
| 用户   | GET    | `/api/v1/users/me`                          | 🔵      |
| 用户   | PUT    | `/api/v1/users/me`                          | 🔵      |
| 用户   | PUT    | `/api/v1/users/me/phone`                    | 🔵      |
| 用户   | GET    | `/api/v1/users/{userId}/reputation`         | 🔵      |
| 商品   | GET    | `/api/v1/products/search`                   | ⚪      |
| 商品   | GET    | `/api/v1/products/{id}`                     | ⚪      |
| 商品   | GET    | `/api/v1/products/{id}/seller-reviews`      | ⚪      |
| 商品   | POST   | `/api/v1/products`                          | 🟢      |
| 商品   | PUT    | `/api/v1/products/{id}`                     | 🟡      |
| 商品   | POST   | `/api/v1/products/{id}/delist`              | 🟡      |
| 商品   | POST   | `/api/v1/products/{id}/mark-sold`           | 🟡      |
| 商品   | GET    | `/api/v1/my/products`                       | 🔵      |
| 订单   | POST   | `/api/v1/orders`                            | 🟢      |
| 订单   | POST   | `/api/v1/orders/{id}/pay`                   | 🟡      |
| 订单   | GET    | `/api/v1/orders/{id}`                       | 🔵      |
| 订单   | POST   | `/api/v1/orders/{id}/confirm-receipt`       | 🟡      |
| 订单   | POST   | `/api/v1/orders/{id}/refund`                | 🟡      |
| 订单   | POST   | `/api/v1/orders/{id}/refund/handle`         | 🟡      |
| 订单   | GET    | `/api/v1/my/orders`                         | 🔵      |
| 聊天   | GET    | `/api/v1/conversations`                     | 🔵      |
| 聊天   | GET    | `/api/v1/conversations/{id}/messages`       | 🔵      |
| 聊天   | POST   | `/api/v1/conversations/{id}/upload-image`   | 🔵      |
| 聊天   | POST   | `/api/v1/conversations/{id}/read`           | 🔵      |
| 评价   | POST   | `/api/v1/reviews`                           | 🔵      |
| 投诉   | POST   | `/api/v1/disputes`                          | 🟢      |
| 投诉   | GET    | `/api/v1/disputes/{id}`                     | 🔵      |
| 投诉   | POST   | `/api/v1/disputes/{id}/comments`            | 🔵      |
| 投诉   | GET    | `/api/v1/my/disputes`                       | 🔵      |
| 通知   | GET    | `/api/v1/notifications`                     | 🔵      |
| 通知   | POST   | `/api/v1/notifications/{id}/read`           | 🔵      |
| 通知   | POST   | `/api/v1/notifications/read-all`            | 🔵      |
| 通知   | GET    | `/api/v1/notifications/unread-count`        | 🔵      |
| 管理   | GET    | `/api/v1/admin/users`                       | 🔴      |
| 管理   | PUT    | `/api/v1/admin/users/{id}/lock`             | 🔴      |
| 管理   | GET    | `/api/v1/admin/products/pending`            | 🔴      |
| 管理   | PUT    | `/api/v1/admin/products/{id}/audit`         | 🔴      |
| 管理   | PUT    | `/api/v1/admin/disputes/{id}/status`        | 🔴      |
| 管理   | GET/POST/DELETE | `/api/v1/admin/sensitive-words`      | 🔴      |
| 管理   | GET    | `/api/v1/admin/statistics`                  | 🔴      |
| 支付   | POST   | `/api/v1/callback/wechat-pay`               | ⚪ 签名 |
| 聊天   | WS     | `/ws/chat`                                  | 🔵      |

---

> **文档结束** | 接口设计覆盖全部用户故事和边界条件。Spring Boot Controller 层建议配合 SpringDoc OpenAPI 自动生成 Swagger UI 文档。