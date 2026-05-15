# .trae 目录结构说明

---

## 概述

`.trae` 是 **Trae IDE** 的工作区配置与文档存储目录。Trae IDE 使用该目录来管理项目级别的规则、配置以及由 AI 助手生成的各类项目文档。

---

## 目录结构

```
.trae/
├── documents/                          # 项目文档存储目录
│   ├── vue3-prototype-plan.md          # Vue3 前端原型实施计划
│   ├── trae-directory-guide.md         # 本文件：.trae 目录结构说明
│   └── backend-documentation-index.md  # 后端文档整合索引
```

---

## 分类说明

### 1. 文档存储目录 — `documents/`

**用途**：存放 Trae IDE 在项目开发过程中生成的项目规划、设计、实施类文档。

**分类**：项目规划与设计文档

| 文件 | 说明 | 类型 |
|------|------|------|
| `vue3-prototype-plan.md` | Vue 3 + Vite 前端可演示界面原型实施计划，包含技术栈选型、目录结构、布局设计、路由设计、10 个页面的功能详述、17 步实施步骤、Mock 数据概要及关键交互效果说明 | 前端原型计划 |
| `trae-directory-guide.md` | 本文件，说明 `.trae` 目录的整体结构及各子目录/文件的作用 | 目录说明文档 |
| `backend-documentation-index.md` | 后端所有 Markdown 文档的整合索引，包含系统架构、数据库设计、API 接口、功能规格、开发任务、验收清单的概要说明及阅读顺序建议 | 后端文档索引 |

---

## 与项目根目录的关系

```
Campus2Hand/                            # 项目根目录
├── .trae/                              # Trae IDE 工作区目录（本目录）
│   └── documents/                      # AI 生成的规划与设计文档
│
├── campus2hand-server/                 # 后端 Java 代码（Spring Boot 模块化单体）
│   ├── campus2hand-common/             # 公共模块
│   ├── campus2hand-auth/               # 认证模块
│   ├── campus2hand-user/               # 用户模块
│   ├── campus2hand-product/            # 商品模块
│   ├── campus2hand-order/              # 订单模块
│   ├── campus2hand-chat/               # 聊天模块
│   ├── campus2hand-review/             # 评价模块
│   ├── campus2hand-dispute/            # 投诉工单模块
│   ├── campus2hand-notification/       # 通知模块
│   ├── campus2hand-job/                # 定时任务模块
│   └── campus2hand-server/             # 启动模块
│
├── 系统架构设计与技术选型.md            # 后端架构设计文档
├── 数据库设计与初始化.md                # 数据库设计文档
├── API接口设计.md                      # API 接口规范文档
├── spec.md                             # 功能规格说明书
├── tasks.md                            # 开发任务分解
└── checklist.md                        # 验收清单
```

---

## 维护说明

- `.trae/documents/` 目录下的文件由 Trae IDE 自动管理，也可手动添加项目规划文档
- 根目录下的 `*.md` 文件为项目核心设计文档，是后端代码实现的依据
- 修改后端代码时应参考根目录下的设计文档，而非 `.trae` 目录下的文件
- `.trae` 目录建议纳入版本控制（Git），以便团队共享 IDE 配置和项目文档