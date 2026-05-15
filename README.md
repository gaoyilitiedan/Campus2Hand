# Campus2Hand 校园二手交易平台

一个专为高校学生设计的校园二手交易平台，支持商品发布、搜索浏览、在线聊天、订单管理、评价系统等完整交易流程。

## 技术栈

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5 | 渐进式 JavaScript 框架 |
| TypeScript | 5.7 | 类型安全的 JavaScript 超集 |
| Vite | 6.2 | 下一代前端构建工具 |
| Vue Router | 4.5 | Vue 官方路由管理器 |
| Axios | 1.16 | HTTP 客户端 |
| Playwright | 1.60 | E2E 自动化测试框架 |

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 运行环境 |
| Spring Boot | 3.2.5 | 微服务基础框架 |
| MyBatis-Plus | 3.5.6 | ORM 持久层框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7.x | 缓存与分布式锁 |
| JWT (jjwt) | 0.12.5 | 无状态身份认证 |
| Maven | 3.x | 项目构建管理 |

## 项目结构

```
Campus2Hand/
├── src/                          # 前端源码
│   ├── api/                      # API 接口封装
│   │   ├── auth.ts               # 认证接口
│   │   ├── index.ts              # 通用接口
│   │   ├── order.ts              # 订单接口
│   │   ├── product.ts            # 商品接口
│   │   └── request.ts            # Axios 请求封装
│   ├── assets/                   # 静态资源
│   ├── components/               # 公共组件
│   │   ├── AppHeader.vue         # 顶部导航栏
│   │   ├── AppSidebar.vue        # 侧边栏
│   │   ├── ImageCarousel.vue     # 图片轮播
│   │   ├── Pagination.vue        # 分页组件
│   │   ├── ProductCard.vue       # 商品卡片
│   │   ├── StarRating.vue        # 星级评分
│   │   └── StatusBadge.vue       # 状态标签
│   ├── layouts/                  # 布局组件
│   ├── mock/                     # Mock 数据
│   ├── router/                   # 路由配置
│   ├── store/                    # Pinia 状态管理
│   ├── utils/                    # 工具函数
│   └── views/                    # 页面组件
│       ├── Home.vue              # 首页（商品搜索浏览）
│       ├── Login.vue             # 登录页
│       ├── ProductDetail.vue     # 商品详情
│       ├── PublishProduct.vue    # 发布商品
│       ├── MyProducts.vue        # 我的商品
│       ├── MyOrders.vue          # 我的订单
│       ├── OrderDetail.vue       # 订单详情
│       ├── MyReviews.vue         # 我的评价
│       ├── MyDisputes.vue        # 我的纠纷
│       ├── Messages.vue          # 消息/聊天
│       └── Profile.vue           # 个人中心
├── tests/                        # Playwright E2E 测试
│   ├── global-setup.ts           # 全局测试初始化（登录态）
│   ├── api.spec.ts               # API 集成测试
│   ├── auth.spec.ts              # 认证模块测试
│   ├── product.spec.ts           # 商品搜索与浏览测试
│   ├── publish.spec.ts           # 商品发布测试
│   ├── order.spec.ts             # 订单模块测试
│   └── profile.spec.ts           # 用户中心测试
├── campus2hand-server/           # 后端微服务模块
│   ├── campus2hand-common/       # 公共模块（异常、工具、配置）
│   ├── campus2hand-auth/         # 认证服务（登录、注册、JWT）
│   ├── campus2hand-product/      # 商品服务（发布、搜索、管理）
│   ├── campus2hand-order/        # 订单服务（创建、支付、状态流转）
│   ├── campus2hand-user/         # 用户服务（信息、信誉分）
│   ├── campus2hand-chat/         # 聊天服务（WebSocket 实时通信）
│   ├── campus2hand-review/       # 评价服务（打分、评论）
│   ├── campus2hand-dispute/      # 纠纷服务（投诉、仲裁）
│   ├── campus2hand-notification/ # 通知服务（消息推送）
│   ├── campus2hand-admin/        # 管理后台服务
│   ├── campus2hand-job/          # 定时任务服务
│   ├── campus2hand-server/       # 服务聚合启动模块
│   ├── seed_mysql.sql            # 数据库初始化脚本
│   └── pom.xml                   # Maven 父 POM
├── API接口设计.md                 # API 接口文档
├── spec.md                       # 功能规格说明书
├── 系统架构设计与技术选型.md        # 架构设计文档
├── 数据库设计与初始化.md            # 数据库设计文档
├── playwright.config.ts          # Playwright 测试配置
├── vite.config.ts                # Vite 构建配置
├── tsconfig.json                 # TypeScript 配置
├── package.json                  # 前端依赖配置
└── index.html                    # 入口 HTML
```

## 环境要求

### 前端开发环境
- **Node.js** >= 18.x
- **npm** >= 9.x

### 后端开发环境
- **JDK** 17
- **Maven** 3.8+
- **MySQL** 8.0+
- **Redis** 7.x

## 快速开始

### 1. 克隆项目

```bash
git clone git@github.com:<your-username>/Campus2Hand.git
cd Campus2Hand
```

### 2. 前端启动

```bash
# 安装依赖
npm install

# 启动开发服务器（默认 http://localhost:3000）
npm run dev

# 生产构建
npm run build

# 预览生产构建
npm run preview
```

### 3. 后端启动

#### 3.1 数据库初始化

```bash
# 登录 MySQL，创建数据库
mysql -u root -p
CREATE DATABASE campus2hand DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入初始化数据
mysql -u root -p campus2hand < campus2hand-server/seed_mysql.sql
```

#### 3.2 配置数据库连接

编辑 `campus2hand-server/campus2hand-server/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus2hand?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
  redis:
    host: localhost
    port: 6379
```

#### 3.3 启动后端服务

```bash
cd campus2hand-server

# 编译打包
mvn clean package -DskipTests

# 启动服务（默认 http://localhost:8080）
cd campus2hand-server/target
java -jar campus2hand-server-1.0.0-SNAPSHOT.jar
```

### 4. 访问系统

- **前端页面**：http://localhost:3000
- **后端 API**：http://localhost:8080

### 5. 测试账号

| 角色 | 学号 | 密码 |
|------|------|------|
| 学生用户 | 2024010101 | Pass1234 |

## 功能模块

### 用户认证
- 学号 + 密码登录
- JWT Token 无状态认证
- Access Token / Refresh Token 双令牌机制
- 密码强度校验（≥8 位，含大小写字母 + 数字）
- 连续登录失败 5 次锁定账户 30 分钟

### 商品管理
- 商品发布（图片上传、标题、描述、价格、分类、新旧程度）
- 商品搜索（关键词、分类、价格区间、排序）
- 商品详情浏览（图片轮播、卖家信息、信誉分）
- 商品状态管理（在售、已下架、已售出）

### 订单系统
- 从商品详情直接下单
- 订单状态流转（待付款 → 已付款 → 已发货 → 已收货 → 已完成）
- 角色切换查看（我买到的 / 我卖出的）
- 订单详情查看

### 聊天系统
- WebSocket 实时通信
- 会话列表管理
- 消息发送与接收

### 评价系统
- 交易完成后双向评价
- 星级评分 + 文字评论
- 信誉分累计

### 纠纷处理
- 提交投诉工单
- 平台仲裁处理
- 纠纷状态跟踪

## 运行测试

项目包含 91 个 Playwright E2E 测试用例，覆盖所有核心功能模块。

```bash
# 运行全部测试
npm test

# 运行指定模块测试
npm run test:api      # API 集成测试
npm run test:e2e      # E2E 功能测试

# 使用 Playwright UI 模式（可视化调试）
npm run test:ui

# 生成并查看 HTML 测试报告
npx playwright show-report
```

### 测试覆盖范围

| 模块 | 测试文件 | 用例数 |
|------|---------|--------|
| API 集成 | `api.spec.ts` | 16 |
| 用户认证 | `auth.spec.ts` | 9 |
| 商品搜索 | `product.spec.ts` | 20 |
| 商品发布 | `publish.spec.ts` | 18 |
| 订单管理 | `order.spec.ts` | 12 |
| 用户中心 | `profile.spec.ts` | 16 |

## API 接口

完整 API 接口文档请参阅 [API接口设计.md](./API接口设计.md)。

### 接口概览

| 模块 | 基础路径 | 说明 |
|------|---------|------|
| 认证 | `/api/v1/auth` | 登录、注册、Token 刷新 |
| 商品 | `/api/v1/products` | 商品 CRUD、搜索 |
| 订单 | `/api/v1/orders` | 订单创建、状态管理 |
| 用户 | `/api/v1/users` | 用户信息、信誉分 |
| 聊天 | `/api/v1/chat` | 会话、消息 |
| 评价 | `/api/v1/reviews` | 评价创建、查询 |
| 纠纷 | `/api/v1/disputes` | 纠纷提交、处理 |

### 统一响应格式

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

| code | 说明 |
|------|------|
| 0 | 成功 |
| 2001 | 参数校验失败 |
| 2002 | 账户已锁定 |
| 4001 | 未登录 |
| 4003 | 无权限 |
| 5000 | 服务器内部错误 |

## 部署

### 前端部署

```bash
# 构建生产版本
npm run build

# dist/ 目录即为静态文件，可部署到 Nginx 或 CDN
```

Nginx 配置示例：

```nginx
server {
    listen 80;
    server_name campus2hand.example.com;

    root /var/www/campus2hand/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 后端部署

```bash
cd campus2hand-server
mvn clean package -DskipTests

# 运行 JAR
java -jar campus2hand-server/campus2hand-server/target/campus2hand-server-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=prod
```

## 相关文档

- [功能规格说明书](./spec.md)
- [API 接口设计](./API接口设计.md)
- [系统架构设计与技术选型](./系统架构设计与技术选型.md)
- [数据库设计与初始化](./数据库设计与初始化.md)

## License

MIT