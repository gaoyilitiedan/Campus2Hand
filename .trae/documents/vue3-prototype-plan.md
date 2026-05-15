# Campus2Hand Vue3 可演示界面原型 — 实施计划

***

## 1. 目标

基于 [spec.md](file:///c:/Users/27355/Desktop/Campus2Hand/spec.md) 功能规格说明书，使用 **Vue 3 + Vite** 构建一个纯前端可演示界面原型。

* **无需后端**：所有数据使用 Mock 静态数据模拟

* **无需状态管理库**：使用 Vue 3 Composition API + reactive/ref 即可

* **布局**：顶部系统名称栏 + 左侧可折叠菜单 + 右侧内容区

* **路由**：Vue Router 4 实现页面切换

***

## 2. 技术栈

| 项    | 选型                      |
| ---- | ----------------------- |
| 框架   | Vue 3 (Composition API) |
| 构建   | Vite 5                  |
| 路由   | Vue Router 4            |
| 样式   | 纯 CSS（CSS Variables）    |
| 图标   | Unicode 符号 / CSS 绘制     |
| Mock | 静态 JSON 数据模块            |
| 语言   | TypeScript              |

***

## 3. 项目目录结构

```
Campus2Hand/
├── index.html
├── package.json
├── vite.config.ts
├── tsconfig.json
├── src/
│   ├── main.ts                     # 入口
│   ├── App.vue                     # 根组件（布局壳）
│   ├── router/
│   │   └── index.ts                # 路由配置
│   ├── mock/
│   │   └── data.ts                 # 所有 Mock 数据
│   ├── layouts/
│   │   └── MainLayout.vue          # 主布局：顶部Header + 左侧Sidebar + 右侧RouterView
│   ├── components/
│   │   ├── AppHeader.vue           # 顶部导航栏（系统名称 + 用户头像）
│   │   ├── AppSidebar.vue          # 左侧菜单
│   │   ├── ProductCard.vue         # 商品卡片
│   │   ├── StarRating.vue          # 星评组件
│   │   ├── ImageCarousel.vue       # 图片轮播
│   │   ├── SearchFilter.vue        # 搜索筛选面板
│   │   ├── MessageBubble.vue       # 聊天气泡
│   │   ├── StatusBadge.vue         # 状态标签
│   │   └── Pagination.vue          # 分页组件
│   ├── views/
│   │   ├── Home.vue                # 首页 - 商品市场（搜索+列表）
│   │   ├── ProductDetail.vue       # 商品详情
│   │   ├── PublishProduct.vue      # 发布商品
│   │   ├── MyProducts.vue          # 我的商品管理
│   │   ├── MyOrders.vue            # 我的订单列表
│   │   ├── OrderDetail.vue         # 订单详情
│   │   ├── Messages.vue            # 聊天消息（会话列表+聊天窗口）
│   │   ├── MyReviews.vue           # 我的评价
│   │   ├── MyDisputes.vue          # 投诉工单
│   │   └── Profile.vue             # 个人中心
│   └── assets/
│       └── style.css               # 全局样式
```

***

## 4. 布局设计

### 4.1 整体布局（MainLayout.vue）

```
┌─────────────────────────────────────────────────┐
│  Campus2Hand 校园二手交易平台        [用户头像]  │ ← AppHeader (h=56px)
├──────────┬──────────────────────────────────────┤
│  📦 商品  │                                      │
│  市场     │                                      │
│  📝 发布  │         <router-view />              │
│  商品     │                                      │
│  📋 我的  │         主内容区                      │
│  商品     │                                      │
│  🛒 我的  │                                      │
│  订单     │                                      │
│  💬 聊天  │                                      │
│  消息     │                                      │
│  ⭐ 评价  │                                      │
│  管理     │                                      │
│  ⚠️ 投诉  │                                      │
│  工单     │                                      │
│  👤 个人  │                                      │
│  中心     │                                      │
├──────────┴──────────────────────────────────────┤
│                   (flex 布局)                     │
└─────────────────────────────────────────────────┘
```

### 4.2 配色方案（CSS Variables）

| 变量名            | 值         | 用途   |
| -------------- | --------- | ---- |
| `--primary`    | `#4A90D9` | 主色   |
| `--success`    | `#52C41A` | 成功   |
| `--warning`    | `#FAAD14` | 警告   |
| `--danger`     | `#FF4D4F` | 危险   |
| `--bg-main`    | `#F5F7FA` | 背景   |
| `--bg-card`    | `#FFFFFF` | 卡片背景 |
| `--text-main`  | `#333333` | 主文字  |
| `--text-muted` | `#999999` | 次要文字 |
| `--border`     | `#E8E8E8` | 边框   |
| `--sidebar-w`  | `200px`   | 菜单宽度 |

***

## 5. 路由设计

| 路径             | 组件                 | 菜单项     | 关联 US          |
| -------------- | ------------------ | ------- | -------------- |
| `/`            | Home.vue           | 📦 商品市场 | US-B01         |
| `/product/:id` | ProductDetail.vue  | （无菜单）   | US-B02, US-B07 |
| `/publish`     | PublishProduct.vue | 📝 发布商品 | US-S01         |
| `/my-products` | MyProducts.vue     | 📋 我的商品 | US-S02         |
| `/my-orders`   | MyOrders.vue       | 🛒 我的订单 | US-B04, US-B05 |
| `/order/:id`   | OrderDetail.vue    | （无菜单）   | US-B04, US-B05 |
| `/messages`    | Messages.vue       | 💬 聊天消息 | US-B03, US-S03 |
| `/my-reviews`  | MyReviews.vue      | ⭐ 评价管理  | US-B06, US-S05 |
| `/my-disputes` | MyDisputes.vue     | ⚠️ 投诉工单 | US-C04         |
| `/profile`     | Profile.vue        | 👤 个人中心 | US-C01\~C03    |

***

## 6. 各页面功能详述

### 6.1 AppHeader.vue（顶部导航栏）

* 左侧：Logo + "Campus2Hand 校园二手交易平台" 文字

* 右侧：用户头像（圆形）+ 昵称 + 下拉（个人中心 / 退出）

* 背景色：`--primary`（深蓝渐变）

### 6.2 AppSidebar.vue（左侧菜单）

* 固定宽度 200px，可折叠至 60px（仅图标）

* 菜单项高亮当前路由（`router-link-active`）

* 每个菜单项：图标 + 文字

* 底部显示当前用户：昵称 + 信誉分

### 6.3 Home.vue — 商品市场（首页）

* **顶部**：搜索框 + 分类标签栏（全部/教材/数码/生活/运动/服饰/其他）

* **筛选面板**：价格区间滑块、新旧程度下拉、校区下拉、排序选择

* **商品列表**：Grid 布局（4列），每列 ProductCard

* **分页**：底部分页组件

* Mock 数据：至少 **30 条**不同商品

### 6.4 ProductCard.vue（商品卡片）

* 商品封面图（缩略图，无图时用纯色占位）

* 标题（1行截断）

* 价格（红色大字）+ 原价划线（可选）

* 新旧程度标签

* 卖家昵称 + 信誉星

* 发布时间（相对时间："3小时前"）

* Hover 阴影效果

### 6.5 ProductDetail.vue（商品详情）

* **图片轮播**：ImageCarousel 组件（最多9张 → Mock 用占位纯色块）

* **右侧信息**：

  * 标题 + 价格（红色）

  * 新旧程度 / 分类 / 校区 标签

  * 完整描述（支持换行）

  * 卖家信息卡片（头像、昵称、信誉分、成交数、评价数）

  * 两个按钮："联系卖家"（跳转聊天）| "立即购买"（跳转下单确认）

* **底部**：卖家评价列表（StarRating + 内容 + 标签）

### 6.6 PublishProduct.vue（发布商品）

* **表单布局**：左右两栏

  * 左栏：图片上传区（拖拽/点击上传，最多9张，网格预览）

  * 右栏：表单字段

    * 标题 input（maxlength=60，实时计数）

    * 描述 textarea（maxlength=2000，实时计数）

    * 售价 input（数字，带 ¥ 前缀）

    * 分类 select（教材/数码/生活/运动/服饰/其他）

    * 新旧程度 radio-group（4个选项带描述）

    * 校区 select

* **底部**：提交按钮（蓝色）+ 取消按钮

* **提交后**：弹出成功 Toast → 跳转"我的商品"

### 6.7 MyProducts.vue（我的商品管理）

* **状态 Tab**：在售 / 审核中 / 已售出 / 已下架（4 个 Tab）

* **列表**：每行一条，含：

  * 缩略图（40x40）+ 标题 + 价格

  * 状态 Badge（不同颜色）

  * 浏览量

  * 操作按钮组：编辑 / 下架 / 标记售出 / 删除（按状态显示）

* **空状态**：插图 + "暂无商品，去发布一个吧\~"

### 6.8 MyOrders.vue（我的订单）

* **两个子 Tab**：我买的 / 我卖的

* **状态筛选**：全部 / 待付款 / 已付款 / 已完成 / 退款中 / 已退款

* **订单卡片**：每笔订单展示：

  * 商品缩略图 + 标题

  * 对方昵称（脱敏）

  * 金额（红色）

  * 状态 Badge（不同颜色对应不同状态）

  * 时间

  * 操作按钮：去支付 / 确认收货 / 申请退款 / 去评价 / 查看详情

* 点击 → 跳转 OrderDetail

### 6.9 OrderDetail.vue（订单详情）

* **订单概要**：商品信息、金额、买卖双方

* **状态时间线**（竖向 Timeline）：

  * 创建时间（待付款）

  * 支付时间（已付款）

  * 确认收货（已完成）/ 退款（已退款）

* **操作区**：根据当前状态显示对应按钮

  * 待付款 → "立即支付" + "取消订单"

  * 待发货 → "申请退款"（仅买家）

  * 已完成 → "去评价"（若未评）

  * 退款中 → 进度提示

* **确认收货二次弹窗**（Modal）

* **申请退款表单**（Modal 内嵌）

### 6.10 Messages.vue（聊天消息）

* **双栏布局**：

  * **左栏（300px）**：会话列表

    * 每项：对方头像 + 昵称 + 关联商品标题（小字）+ 最后消息摘要 + 时间 + 未读角标

    * 选中高亮

  * **右栏**：聊天窗口

    * **顶部**：对方昵称 + 关联商品（可点击跳转）

    * **中间**：消息列表（MessageBubble），自动滚到底部

      * 自己消息：右侧蓝色气泡

      * 对方消息：左侧灰色气泡

      * 时间戳居中

    * **底部**：输入框 + 发送按钮 + 图片按钮

* **Mock**：预设 3-5 个会话，每个含 10-20 条历史消息

* **模拟发送**：输入文字点击发送 → 立即追加到自己消息列表，1秒后模拟对方回复

### 6.11 MyReviews.vue（评价管理）

* **两个 Tab**：我收到的评价 / 我给出的评价

* **评分概览**：信誉分大数字 + 星评分布柱状图（CSS绘制）

* **评价列表**：每条含：

  * 评价人头像 + 昵称

  * 关联商品（可点击）

  * StarRating + 文字内容 + 标签

  * 时间

### 6.12 MyDisputes.vue（投诉工单）

* **工单列表**：卡片式

  * 工单编号 + 投诉类型标签

  * 关联订单（可点击）

  * 投诉描述摘要

  * 状态标签：待受理 / 处理中 / 已解决 / 已驳回

  * 创建时间

* **工单详情 Modal**：

  * 完整描述 + 凭证图片

  * 留言记录列表（双方对话）

  * 追加留言输入框（处理中状态可追加）

### 6.13 Profile.vue（个人中心）

* **上部卡片**：头像 + 昵称 + 信誉分（⭐ 大数字）+ 校区

* **统计区**：3 列数字 —— 发布数 / 成交数 / 评价数

* **信息列表**：

  * 学号（脱敏展示 `20****01`）

  * 校园邮箱（脱敏 `z****n@campus.edu`）

  * 手机号（仅本人可见，脱敏 `138****5678`）

  * 校区

  * 注册时间

* **操作按钮**：修改密码 / 切换校区 / 退出登录

***

## 7. 实施步骤（按顺序）

| 步骤  | 任务                          | 说明                                       |
| --- | --------------------------- | ---------------------------------------- |
| S1  | 初始化 Vue3 + Vite + TS 项目     | `npm create vite@latest` → 安装 vue-router |
| S2  | 创建全局样式 `style.css`          | CSS Variables、reset、通用布局类                |
| S3  | 创建 Mock 数据模块 `mock/data.ts` | 30+商品、3-5会话、8+订单、评价、工单全部Mock数据           |
| S4  | 创建布局组件 MainLayout           | flex 布局：顶部固定 + 左侧固定 + 右侧滚动               |
| S5  | 创建 AppHeader + AppSidebar   | 导航栏 + 菜单路由                               |
| S6  | 配置 Vue Router               | 10 条路由 + beforeEach 守卫（模拟登录拦截）           |
| S7  | 实现 Home.vue + ProductCard   | 首页商品市场（搜索+筛选+列表+分页）                      |
| S8  | 实现 ProductDetail.vue        | 商品详情 + ImageCarousel + StarRating        |
| S9  | 实现 PublishProduct.vue       | 发布商品表单                                   |
| S10 | 实现 MyProducts.vue           | 商品管理（状态Tab + 操作）                         |
| S11 | 实现 MyOrders.vue             | 订单列表（角色Tab + 状态筛选）                       |
| S12 | 实现 OrderDetail.vue          | 订单详情 + 确认收货弹窗 + 退款表单                     |
| S13 | 实现 Messages.vue             | 聊天双栏（会话列表 + 聊天窗口）                        |
| S14 | 实现 MyReviews.vue            | 评价管理 + 信誉展示                              |
| S15 | 实现 MyDisputes.vue           | 工单列表 + 详情弹窗 + 留言                         |
| S16 | 实现 Profile.vue              | 个人中心（信息脱敏展示）                             |
| S17 | 联调测试 + 整体走查                 | 所有页面跳转、表单交互、Mock数据完整性                    |

***

## 8. Mock 数据概要

### 8.1 当前登录用户

```ts
currentUser = {
  id: 'u_001', studentId: '2024010101', nickname: '张三',
  avatar: '', campus: '本部校区', email: 'zhangsan@campus.edu',
  phone: '13812345678', reputationScore: 4.8,
  totalSold: 12, totalBought: 5, reviewCount: 18
}
```

### 8.2 商品列表（30+条）

涵盖 6 大分类，混合不同新旧程度和校区，每个商品关联一个卖家信息。

### 8.3 订单列表（8+条）

覆盖全部状态：pending\_payment / paid\_escrow / completed / refunding / refunded，分买家/卖家两种视角。

### 8.4 聊天会话（3-5个）

每个会话关联一个商品和对方用户，含 10-20 条历史消息。

### 8.5 评价（20+条）

关联到各用户和商品，含不同星级分布。

### 8.6 投诉工单（2-3个）

覆盖不同状态：pending / processing / resolved。

***

## 9. 关键交互效果

| 效果         | 实现方式                                         |
| ---------- | -------------------------------------------- |
| 路由切换动画     | `<Transition>` fade                          |
| 菜单折叠       | sidebar 宽度 toggle + CSS transition           |
| 商品卡片 Hover | `transform: translateY(-4px)` + `box-shadow` |
| 图片轮播       | CSS `scroll-snap` + 指示点                      |
| 星评组件       | CSS Unicode ★ 填充/空心                          |
| 模态弹窗       | Teleport + overlay                           |
| Toast 提示   | 固定定位 + setTimeout 自动消失                       |
| 模拟对方回复     | `setTimeout` 1s 后追加消息                        |
| 表单实时字符计数   | computed + v-model                           |
| 空状态占位      | 居中文字 + emoji                                 |

***

> **预计交付物**：一个可直接 `npm run dev` 启动的 Vite + Vue 3 项目，包含上述全部 10 个页面、Mock 数据、交互效果，可在浏览器中完整演示注册→浏览→购买→聊天→评价→投诉的全流程。

