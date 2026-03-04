# 🎓 BMAD Edge Gateway — 区域教育增值评价大数据指挥舱

> **政务级 · 全域增值评价 · 智能预警 · 安全脱敏开放**

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4-42b883?logo=vuedotjs)](https://vuejs.org/)
[![Redis](https://img.shields.io/badge/Redis-7-dc382d?logo=redis)](https://redis.io/)
[![License](https://img.shields.io/badge/License-Private-blue)]()

---

## 📖 项目简介

**BMAD Edge Gateway** 是一套面向地市级教育行政部门打造的 **区域教育增值评价大数据指挥舱系统**。它融合了从数据采集、算法推演、大屏可视化到对外 API 开放的全链路能力，旨在帮助教育管理者精准把握区域内各校的五育发展态势，发现异常、诊断归因并推荐改进对策。

系统严格遵循 **等保三级** 安全基线要求，实现了多租户行级数据隔离、操作审计不可篡改、脱敏输出等核心安全机制。

---

## 🏗️ 系统架构

```
┌──────────────────────────────────────────────────────┐
│                    前端 (Vue 3)                       │
│   Vite · Ant Design Vue · ECharts · Tailwind CSS     │
│   SSE 实时告警 · 沙箱推演面板 · CMS 管理 · 雷达大屏   │
└────────────────────┬─────────────────────────────────┘
                     │ RESTful API / SSE
┌────────────────────▼─────────────────────────────────┐
│              后端网关 (Spring Boot 3)                  │
│   Spring Security + JWT · MyBatis-Plus 多租户隔离     │
│   AOP 审计切面 · Redis 缓存 · 分布式限流 (Lua)        │
│   OpenAPI 脱敏网关 · 异步沙箱引擎                     │
└────────┬───────────────────────┬──────────────────────┘
         │                       │
    ┌────▼────┐            ┌─────▼─────┐
    │  MySQL  │            │   Redis   │
    │  (H2)   │            │  缓存/限流  │
    └─────────┘            └───────────┘
```

---

## ✨ 核心功能 (Epic 概览)

### Epic 1 — 安全基座与分级权限接入
- 🔐 政务 SSO 单点登录 + JWT 无状态鉴权
- 👥 RBAC 多级角色管理（市级 / 区县 / 校级 / 运维）
- 🛡️ MyBatis-Plus `TenantLineInnerInterceptor` 行级数据隔离

### Epic 2 — 大屏指挥舱与异常雷达
- 📊 全市区县级宏观热力图（Redis 缓存加速 < 3s 首屏）
- 🔴 ECharts 聚光异常图谱（红色脉冲动效 + 悬停聚焦遮罩）
- ⚡ SSE 实时推送突发预警（不可自动关闭的强通知框）
- 🧩 智能诊断抽屉（手风琴归因 + 置信度评分 + 骨架屏过渡）
- 📚 经验智库引荐卡片（AI 强推徽章 + 侧边预览）

### Epic 3 — 归因沙箱与底库防线
- ⏰ 模拟 XXL-JOB 定时跑批（断点续传 + 3 次重试告警）
- ✏️ 人工断点数据补录（Excel 批量导入 + 逻辑校验拦截）
- 📝 AOP 审计切面（不可篡改的操作日志，双写至 `edu_audit_log`）
- 🧪 沙箱算法推演（异步线程池 + 长轮询进度反馈）
- 🚀 一键发布大基线（版本号自增 + 缓存全量刷新）
- 📰 经验对策库 CMS 富文本管理

### Epic 4 — 开放协同中台
- 🌐 Open API 脱敏画像输出（`/open-api/v1/assessment/schools/{id}`）
- 🔑 独立 Token 鉴权体系（`OpenApiAuthFilter`，与内部 JWT 完全隔离）
- 🚧 分布式限流盾（AOP + Redis Lua 原子脚本，超限返回 HTTP 429）

---

## 🚀 快速启动

### 环境要求

| 组件 | 版本要求 |
|------|---------|
| JDK | 17+ |
| Maven | 3.8+ |
| Node.js | 18+ |
| pnpm | 9.0+ |
| Redis | 6+ (限流功能需要) |
| Docker | 可选 (用于快速启动 Redis) |

### 1. 启动 Redis（限流功能依赖）

```bash
# 方式一：Docker 快速启动
docker run -d -p 6379:6379 --name bmad-redis redis

# 方式二：本地 Redis 服务
redis-server
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端将在 `http://localhost:8080` 启动，嵌入式 H2 数据库自动初始化。

### 3. 启动前端

```bash
cd frontend
pnpm install
pnpm dev
```

前端将在 `http://localhost:3000` 启动，自动代理 API 请求至后端。

### 4. 一键验证脚本

```powershell
# Windows PowerShell
.\run_demo.ps1
```

---

## 📂 项目结构

```
bmad-test/
├── backend/                          # 后端 Spring Boot 3 工程
│   └── src/main/java/com/bmad/edge/
│       ├── annotation/               # 自定义注解 (@RateLimit, @Audit)
│       ├── aspect/                   # AOP 切面 (限流盾, 审计日志)
│       ├── config/                   # 配置类 (Security, WebMvc, MyBatis)
│       ├── controller/               # REST 控制器 (9个)
│       ├── dto/                      # 数据传输对象
│       ├── entity/                   # 数据库实体
│       ├── mapper/                   # MyBatis-Plus Mapper
│       ├── security/                 # 安全组件 (JWT, SSO, OpenApiAuth)
│       └── service/                  # 业务服务层
├── frontend/                         # 前端 Vue 3 + Vite 工程
│   └── src/
│       ├── views/                    # 页面视图
│       │   ├── dashboard/            # 大屏指挥舱
│       │   ├── admin/                # 管理后台 (沙箱/补录/CMS)
│       │   └── login/                # 登录页
│       ├── composables/              # 组合式函数
│       ├── api/                      # API 请求封装
│       └── router/                   # 路由配置
├── deploy/                           # 部署配置
├── docs/                             # 项目文档
└── _bmad-output/                     # BMAD 规划产出物
    └── planning-artifacts/           # PRD, 架构, UX, Epics
```

---

## 🔒 安全机制

| 安全项 | 实现方式 |
|--------|---------|
| 身份鉴权 | Spring Security + JWT (HS256) |
| 数据隔离 | MyBatis-Plus 多租户拦截器 (SQL 透明注入) |
| 操作审计 | AOP `@Audit` 切面，不可篡改双写 |
| 对外认证 | `OpenApiAuthFilter` 独立 Token 体系 |
| 流量防护 | `RateLimitAspect` + Redis Lua 原子限流 |
| 数据脱敏 | 独立 DTO 层裁剪，禁止源数据外泄 |

---

## 🛠️ 技术栈

**后端：**
- Spring Boot 3.2 · Spring Security · MyBatis-Plus 3.5
- Redis (缓存 + 分布式限流) · H2 (开发环境嵌入式数据库)
- Lombok · SpringDoc OpenAPI 3

**前端：**
- Vue 3.4 · Vite 5 · Vue Router 4 · Pinia
- Ant Design Vue 4 · ECharts 6 · Tailwind CSS 3
- Axios · TypeScript

---

## 📡 API 速查

### 内部 API (需 JWT 登录)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/system/login` | 用户登录获取 JWT |
| GET | `/api/v1/dashboard/macro/heat-map` | 全域热力图数据 |
| GET | `/api/v1/dashboard/radar/{scope}` | 雷达图谱数据 |
| GET | `/api/v1/alerts/stream` | SSE 实时告警推送 |
| POST | `/api/v1/sandbox/config` | 提交沙箱参数 |
| POST | `/api/v1/sandbox/run` | 触发异步试跑 |
| GET | `/api/v1/sandbox/status/{taskId}` | 轮询试跑进度 |
| POST | `/api/v1/sandbox/publish` | 发布至生产基线 |
| POST | `/api/v1/data-fallback/submit` | 补录断点数据 |
| GET/POST | `/api/v1/articles/**` | CMS 文章管理 |

### 开放 API (需 Open-Token)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/open-api/v1/assessment/schools/{id}` | 脱敏画像查询 |

**鉴权方式：** 请求 Header 携带 `Open-Token: <your-token>`

---

## 📜 许可证

本项目为内部政务系统，未经授权禁止外部使用与分发。

---

<p align="center">
  <b>BMAD Edge Gateway</b> · 用数据驱动教育公平
</p>
