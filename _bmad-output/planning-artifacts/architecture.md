---
stepsCompleted: ["step-01-init", "step-02-context", "step-03-starter", "step-04-decisions", "step-05-patterns", "step-06-structure", "step-07-validation"]
inputDocuments: ["prd.md"]
workflowType: 'architecture'
project_name: 'bmad-test'
user_name: 'centron'
date: '2026-03-03'
---

# Architecture Decision Document

_This document builds collaboratively through step-by-step discovery. Sections are appended as we work through each architectural decision together._

## Project Context Analysis

### Requirements Overview

**Functional Requirements:**
系统核心是由 21 项 FR 构成的“计算-配置-展示”闭环。最核心的挑战在于分离两套截然不同的业务流：教研员高权限、低频但消耗算力的“模型调参验证流”（沙箱），以及各级教育局、校长高频查看的“增量体检大屏展示流”。另外，强依赖人工补录与审计（FR5/FR6）来保证数据的可用性。

**Non-Functional Requirements:**
架构设计将被两项关键 NFR 驱动：
1. **合规性 (NFR1-NFR4)**: 等保三级、SM4 加密、国密标准及严格脱敏，要求在基础设施和数据持久层必须有企业级的安全网关与审计机制。
2. **性能及稳定性 (NFR5-NFR9)**: 面对前端 <3s 首屏及 API 100 QPS 下 <500ms 的响应要求，系统必须具备强大的缓存策略和读写分离能力，绝不能在高峰期透传复杂计算。

**Scale & Complexity:**
- Primary domain: Full-Stack SaaS (Vue/React 前端大屏 + Java/Node.js 业务中台 + Python 算法引擎)
- Complexity level: High (政务私有云部署，复杂的数据可见性隔离，多源异构整合)
- Estimated architectural components: 至少包含前端聚合网关、SSO 认证中心、主业务 CRUD 后台、API 开放中台、异步任务调度器、Python 算法微服务及沙箱环境。

### Technical Constraints & Dependencies

- **基础设施**: 必须支持完全内网/政务私有云物理部署，对外断网。
- **外部依赖**: 强依赖前置的“教育数据中心”以准实时/跑批方式提供基础测评数据，不可直连原始数据源。依赖外部统一 SSO 认证。

### Cross-Cutting Concerns Identified

- **逻辑多租户与数据隔离**: 行级别 (Organization/District ID) 的 RBAC 权限过滤必须横跨所有 API 强制生效（市-区-校三级网格）。
- **可追溯性与容错**: 前置数据的脆弱性要求所有计算及模型修正动作具有全量审计日志 (Audit Log)。
- **CQRS (命令与查询职责分离) 架构心智**: 
  - *读核心 (C端与API)*：高并发大屏和中间层 API 强制读取计算完毕的预落库“只读副本”（由定时任务与 Redis 支撑）。
  - *写核心 (M端沙箱)*：权重调涉及即时、耗时的隔离算法算力，必须基于异步消息队列调度 Python 引擎进行“沙盘推演”并独立回写。

## Starter Template Evaluation

### Primary Technology Domain

基于项目需求与用户偏好，项目被划分为 **Full-Stack SaaS (前后端分离)**。
技术偏好明确：Java (Spring Boot) + MySQL (后端与网关) / Vue (前端大屏) / K8s (部署) / Python (算法微服务)。

### Starter Options Considered

在面对企业级政务系统（等保三级、SSO、高强度 RBAC）时，我们考察了以下主流的 Spring Boot + Vue 3 启动模板：

1. **JeecgBoot (Vue 3 + Spring Boot 3)**:
   - *特点*：极度成熟的企业级低代码平台，自带极强的代码生成器和工作流引擎。
   - *缺点*：过于庞大与厚重 (Monolith心智)。对于我们要实施严格的 CQRS（读写分离算法引擎）以及精细的大屏定制来说，剥离其无用组件的成本极高。
2. **Vue Vben Admin (Vue 3 + Vite + TypeScript) / 配合纯净 Spring Boot 3**:
   - *特点*：当前 Vue 3 生态中最强大的中后台模板之一（目前是 v5 版本）。内建优秀的路由控制、请求封装和多主题。配合一套纯粹的 Spring Boot 3 (JDK 17/21) RESTful Boilerplate，能够给予开发团队最大的自由度去打磨架构。

### Selected Starter: Vben Admin v5 + Spring Boot 3 (独立分离架构)

**Rationale for Selection:**
政务大屏项目对前端的可定制性、各类 ECharts 复杂图表的集成以及动画性能要求极高；同时后端面临内网数据安全和 API 限流挑战。放弃大包大揽的低代码框架，选择前端用极致成熟的 `vben-admin` 搭建大屏与后管沙箱，后端用纯净的 `Spring Boot 3` 搭建无状态微服务网关，是应对复杂多变业务的最稳妥基石。

**Initialization Command:**

```bash
# 前端 (Vue 3 + Vite + Element Plus/Ant Design)
pnpm create vben-admin my-education-dashboard

# 后端 (Spring Boot 3 + Spring Security + MyBatis-Plus)
curl https://start.spring.io/starter.zip \
  -d dependencies=web,data-jpa,security,mysql,data-redis,actuator \
  -d language=java \
  -d javaVersion=21 \
  -d type=maven-project \
  -d bootVersion=3.2.x \
  -o edge-gateway.zip
```

**Architectural Decisions Provided by Starter:**

**Language & Runtime:**
- 前端：TypeScript 5.x + Vue 3 (Composition API) + Node.js (构建环境)
- 后端：Java 21 + Spring Boot 3.2.x + MySQL 8 + Redis (缓存与限流基座)

**Styling Solution:**
- Tailwind CSS (Vben 内置) 配合 UI 组件库 (Element Plus 或 Ant Design Vue)，兼顾大屏快速排版与精细化定制。

**Build Tooling:**
- 前端：Vite (提供极速的冷启动与 HMR 热重载)。
- 后端：Maven (结合政务内网私服配置)。

**Testing Framework:**
- 前端：Vitest + Vue Test Utils。
- 后端：JUnit 5 + Mockito + Testcontainers (用于数据库黑盒测试)。

**Code Organization:**
- 前端：采用 Vben 推荐的 Monorepo 或标准 `src/views`, `src/api`, `src/store` (Pinia) 隔离。
- 后端：DDD (领域驱动设计) 分层架构，严格区分 `Controller(API网关)` -> `Service(业务组装)` -> `Repository(数据防腐层)`，以满足等保三级的数据隔离要求。

**Development Experience:**
- Docker & K8s 原生友好：前后端均提供标准化 `Dockerfile` 与 Helm Charts 骨架，完美契合政务云的 K8s 裸机部署方案。

**Note:** Project initialization using this command should be the first implementation story.

## Core Architectural Decisions

### Decision Priority Analysis

**Critical Decisions (Block Implementation):**
- Authorization & Authentication Strategy
- API Gateway & Inter-Service Communication Pattern
- Data Architecture (Database & Caching)

**Important Decisions (Shape Architecture):**
- Frontend Data Fetching Paradigm

### Authentication & Security

- **Category**: Security
- **Decision**: Spring Security + OAuth2 Resource Server (JWT)
- **Version**: Spring Security 6.x (with Spring Boot 3.2.x)
- **Rationale**: 政务私有网要求对接 SSO，采用基于非对称加密的 JWT Token 配合 Spring Oauth2 解析栈，能够无痛支撑纯前后的 Vben Admin 路由鉴权（无状态）。在 Java 端辅以 `@PreAuthorize` 配合 AOP 拦截器完成市/区/校三级 RBAC 硬隔离。
- **Affects**: API Gateway, Frontend Auth Flow, User Management

### API & Communication Patterns

- **Category**: Inter-Service Communication (Java <-> Python)
- **Decision**: 异步消息队列 (基于 Redis Stream/PubSub)
- **Rationale**: 算法沙箱的聚类验证通常极度耗时（可能是几分钟级别）。采用轻量级的 Redis (List/PubSub/Stream) 作为 Java API 与后台 Python Compute 服务之间的媒介，完美解耦，无需部署沉重的 Kafka/RabbitMQ，最大限度降低等保环境下的运维复杂度。
- **Affects**: Python Algorithm Service, Java Task Dispatcher, Redis Cluster

- **Category**: Frontend-Backend Data Sync
- **Decision**: Server-Sent Events (SSE) / WebSocket
- **Rationale**: 针对核心指标暴跌等红灯告警，采用 SSE (单向推送) 或 WebSocket 让 Spring Boot 主动推送到 Vue 大屏。对于一般性的排名查看，保留标准的 RESTful JSON 轮询即可。
- **Affects**: Vue Dashboard Components, Spring WebFlux/WebSocket Config

### Data Architecture

- **Category**: MySQL Multi-Tenancy Strategy (逻辑多租户)
- **Decision**: Shared Database, Shared Schema (单库单表 行级硬隔离)
- **Version**: MySQL 8.x
- **Rationale**: 租户细分到市-区-校三级网格，按 Schema 分库会导致 DDL 维护成本呈指数级上升。采用单库单表，配合 MyBatis-Plus 的 `TenantLineInnerInterceptor` 插件在底层自动拼装 `district_id` 与 `school_id` 进行透明拦截，既满足等保要求的数据隔离，又极大降低了开发与运维难度。
- **Affects**: Database Schema Design, MyBatis-Plus Configuration, Data Migration

- **Category**: Hot Data Caching Strategy (热点缓存驱动)
- **Decision**: Redis JSON/Hash 完全接管读流量
- **Version**: Redis 7.x
- **Rationale**: 为了在高峰期满足前端首屏 <3s 及 API 100并发下 P95 <500ms 的硬性指标，大屏和中台绝对不能穿透到 MySQL 进行聚合计算。由 Python 定时将算好的“热力图/雷达图”全量结构化塞入 Redis，Java 仅做薄封装与透传取值。
- **Affects**: API Response Time, Redis Cluster Sizing, Python Data Pipeline

### Infrastructure & Deployment

- **Category**: Batch Job Orchestration (任务调度中心)
- **Decision**: XXL-JOB (或轻量化 Apache DolphinScheduler)
- **Rationale**: 系统强依赖前置“教育数据中心”定时供数，并唤起 Python 算法聚类。引入成熟的独立调度中心来管控 Java 抽数和 Python 算力的串联，不仅满足 NFR8 (失败 3 次独立告警及断点续传)，更能清晰留存每次跑批的审计日志。
- **Affects**: Deployment Topology, Task Scheduling, Error Alerting

### Decision Impact Analysis

**Implementation Sequence:**
1. **Infrastructure Setup**: 初始化 Vben Admin 与 Spring Boot 3 工程骨架，搭建 MySQL 8 与 Redis 7 基础设施。
2. **Security & Auth Core**: 跑通基于 SSO 的 Jwt Token 解析，以及基于 `TenantLineInnerInterceptor` 的多租户拦截底座。
3. **Data Pipeline & Scheduler**: 部署 XXL-JOB，打通 Java 从前置中心抽数落库的通道。
4. **Inter-Service Mesh**: 建立基于 Redis Stream/PubSub 的 Java-Python 握手通信。
5. **Compute & Cache**: 核心 Python 增值算法引擎试算，并将结构化结果装载进 Redis。
6. **Dashboard Development**: 大屏前端对接 Redis 预加载数据，完成市/区/校三级交互与流转。

**Cross-Component Dependencies:**
- 前端 (Vben Admin) 的路由鉴权高度依赖后端 (Spring Security OAuth2) 下发的 JWT Claims 中包含的政务层级标签 (市/区/校)。
- 大屏响应速度 (NFR5, NFR9) 完全系于 Python 引擎是否及时将预计算好的 JSON 塞入 Redis，Java 的责任仅为权限校验与快速透传。

## Implementation Patterns & Consistency Rules

### Pattern Categories Defined

**Critical Conflict Points Identified:**
为了防止后续多个 AI Agent 在前端 (Vben/Vue3)、中台 (Spring Boot 3/Java)、后台 (算法/Python) 跨语言开发时产生“精神分裂”与协作冲突，我们定义了以下 4 个必须强制遵守的实施铁律。

### Naming Patterns (命名规范)

**Database Naming Conventions:**
- 表名采用小写下划线，带业务前缀：`edu_school`, `edu_indicator_record`。
- 列名采用小写下划线：`school_id`, `create_time`。
*(反模式：避免使用混合大小写如 `eduSchool` 或不加前缀的泛化词组如 `user`)*

**Code Naming Conventions:**
- **Entity/DTO (Java/TS)**: 强制使用大驼峰映射（如 `SchoolEntity`）。
- **字段/属性名 (Java/TS)**: 强制前端 JSON 交互与 Java 属性定义使用小驼峰（如 `schoolId`）。由框架层拦截转换为数据库下划线格式，严禁出现下划线越界到代码逻辑层。
- **Component (Vue)**: 大驼峰命名文件及组件 `SchoolRadarChart.vue`。

### Structure Patterns (结构与路由)

**API Naming Conventions:**
- 全局强制实施标准 RESTful 风格，并通过版本号控制。
- 格式规范：`/{api-version}/{模块}/{资源}/{id}`
- 正确示例：`GET /api/v1/assessment/schools/{id}` (获取明细), `POST /api/v1/assessment/schools/{id}/appeal` (提交申诉，允许利用动词作为子资源操作)。
*(反模式：严禁使用 `POST /api/getSchoolById?id=123` 这种非标准动词化 URI)*

### Format Patterns (格式规范)

**API Response Formats (统一响应拦截):**
- **强制使用状态码 200 结合自定义 Code 的结构体**。由于政务网络的复杂性，依靠 HTTP 4xx/5xx 常被网关拦截或丢弃。
- **标准结构**：
  ```json
  {
    "code": 200,      // 代表业务成功，或是 4001 代表Token失效，5001 代表后台结算中
    "message": "success",
    "data": { ... }   // 具体的业务负载
  }
  ```
- Any error must still return HTTP 200 with the `code` specifying the error, allowing Vben Admin frontend interceptors to uniformly handle warnings/re-logins.

**Data Exchange Formats (时间戳处理):**
- 跨语言传输（特别是 Java <=> Python <=> Vue）的时间**一律使用字符串类型** `YYYY-MM-DD HH:mm:ss`，禁止传输长整型 Timestamp。
- Java 端需通过全局 Jackson 配置 `GMT+8` 时区保证反序列化不产生偏差。

### Enforcement Guidelines

**All AI Agents MUST:**
- 严格遵循 `code`, `message`, `data` 统一包装结构来设计 Controllers 和 Axios 调用。
- 绝不在响应中直接暴露数据库主键生成器，一切前端传递和展示的主键必须安全封装。
- JSON 键名永远保持为小驼峰，即便是在处理 Python 返回的模型结果时也必须进行 Key 重命名处理。

## Project Structure & Boundaries

### Complete Project Directory Structure

我们采用严谨的 Monorepo 工程结构管理，在融合了多 AI Agent 视角（DevOps、测试与核心开发）的探讨后，设计出以下支持高并发协同、清晰定界的物理目录树。

```text
bmad-test/
├── .github/workflows/       # 编排按目录 Path 隔离的 CI/CD Pipeline (Java/Node/Python 各自独立触发)
├── docs/
│   ├── api-contracts/       # 存放 Java 与 Python 之间通过 Redis 异步通信的 JSON Schema 和 AsyncAPI 文档，作为绝对真理的契约
│   └── architecture.md
├── frontend/                # [Vben Admin v5] 前端工程 (Vue3/Vite/TS)
│   ├── src/views/dashboard/         # 市/区/校三级大屏与热力图页面 (FR10-FR14)
│   ├── src/views/assessment/        # 指标沙箱与参数配置界面 (FR7-FR9)
│   ├── src/views/admin/             # 用户权限、系统设置及补录修正 (FR1-FR6)
│   ├── src/api/                     # 严格包装的 Axios 接口调用层
│   ├── src/store/                   # Pinia 状态树 (包含 JWT 和深层级权限打标)
│   └── tests/                       # Vitest 前端单元测试及大屏组件渲染快照测试
├── backend/                 # [Spring Boot 3] 主业务安全网关 (Java 21)
│   ├── src/main/java/.../controller/ # 暴露给 Vben 页面拉取数据的 REST 及 SSE 断点
│   ├── src/main/java/.../service/    # 权限校验、查询封装、XXL-JOB 分发、JSON 透明透传逻辑
│   ├── src/main/java/.../repository/ # MyBatis-Plus 含多租户拦截功能的防腐层
│   ├── src/main/java/.../security/   # Spring Security OAuth2 (对接近 SSO) 及 Method 级 AOP 鉴权
│   ├── src/test/java/                # Java 端 JUnit5 和 Mockito 测试套件
│   └── pom.xml
├── algorithm/               # [Python 微服务] 核心增值算法及数据清洗流水线
│   ├── core/                        # 同类分层、K-Means / 聚类沙箱推演算法实体
│   ├── queues/                      # 对接 Redis Pub/Sub 与长列队消费者的 Wrapper
│   ├── tests/                       # PyTest 算法边界回归测试，防范因为新数据结构导致旧指标崩溃
│   ├── app.py                       # 极简启动入口点 (如 FastAPI 或纯 Worker)
│   └── requirements.txt
└── deploy/                  # [DevOps 本地与政务云环境]
    ├── docker-compose.yml           # 支持开发者一键冷启动全套本地 MySQL 8, Redis 7, XXL-JOB
    ├── mysql-init/                  # 包含基础政务级字典与鉴权路由的基础 SQL 导入脚本
    └── k8s-manifests/               # 用于政务私有云直接 `kubectl apply` 的裸机发布 Config
```

### Architectural Boundaries

**沟通边界 (Service Integration Patterns)**:
- **严格隔断前端与算力节点**：Frontend 永远不允许跨过 Backend 网关直接给 Python Algorithm 下发任何请求或暴露连接端口；
- **异步数据母线**：Backend 与 Python Algorithm 的所有指令与处理进度汇报，只能透过内部网络架构核心的 Redis Stream / Queue 层完成非阻塞传递；在模型运行完成产生数百 KB 的指标打分结果后，Python 直接落盘进 Redis，通知 Java 取用即可。

**跨职能测试与 CI/CD 边界**:
- 所有的编译单元 `build/test` 在 GitHub Action (或局域网 Gitlab CI) 完全依靠 Git 变更的子路径探测。前端改 UI，绝不拉起 Java 和 Python 的庞大测井集。

### Requirements to Structure Mapping

- **Epic: 大屏可视化引擎 (FR10-14)**
  - UI 落地：`frontend/src/views/dashboard/` 
  - 数据缓存读取：`backend/src/main/java/.../service/DashboardServiceImpl.java` 
- **Epic: 算法沙箱及管理员录入 (FR4-9, FR15-16)**
  - 界面：`frontend/src/views/assessment/` 和 `frontend/src/views/admin/`
  - 逻辑算力：`algorithm/core/` 
- **Epic: 多租户 SSO 安全及数据隔离 (FR1-3, NFR1-4)**
  - OAuth 令牌拦截：`backend/src/main/java/.../security/`
  - 物理 SQL 过滤：`backend/src/main/java/.../repository/` (MyBatis-Plus Interceptor)

## Architecture Validation Results

### Coherence Validation ✅

**Decision Compatibility:**
- Vue 3 (Vben) + Java 21 (Spring Boot 3) + Python 算力的三点支撑非常稳固。前后端由 REST/SSE 解耦，计算密集型与 IO 密集型由 Redis Queue 彻底解耦。
- 无任何相互冲突的框架版本，技术栈成熟且社区支持强大。

**Pattern Consistency:**
- 统一的 `{ code: 200, data, message }` 响应体和全局字符串日期格式，根除了 90% 的前端 Axios 拦截与联调报错风险。
- Vben Admin 强大的 Axios 拦截包装完美匹配 `code: 200` 软错误处理规范。

**Structure Alignment:**
- 物理目录完全支持了上述决策，特别是针对 CI/CD 边界划分的文件编排（分别独立管理）。

### Requirements Coverage Validation ✅

**Feature Coverage:**
- **大屏引擎与动态图表**: 大量前端定制视图将存放在 `dashboard/` 下，高并发访问依靠后端 Redis 预取策略缓存。
- **算法沙箱配置**: 大篇幅录入型 UI 放于 `assessment/`，后端提供极简入库，算力依赖 Python 的 `core/` 与长队列。

**Non-Functional Requirements Coverage:**
- **权限与隔离的等保三级基线 (NFR1-4)**: Spring Security OAuth2 负责登录凭据，AOP 守护 Method，MyBatis-Plus `TenantLineInnerInterceptor` 守护最底层的查询边界，实现铁桶防御。
- **响应速度保障 (NFR5, NFR9)**: 严密解耦了算法的高延时；纯文本/图表数据必须从 Redis 热缓存里按需拖取。

### Implementation Readiness Validation ✅

**Completeness Assessment:**
- 指导完备度极高。包括数据库/对象命名的小驼峰/下划线转化策略，也具备。
- 物理层甚至安排好了专用的 `tests/` 目录给未来的 TDD 提供落脚点。

### Documentation Supplements (The 1% Gap Completed)

- 前后端互联的最后一块拼图确立：**接口文档将使用 Swagger (基于 SpringDoc OpenAPI 3)** 自动生成。它将在 Java 端被深度集成，并作为 Vben 前端开发人员获取 RESTful 路由的官方查阅词典。Python 侧则用 FastAPI 内置的 Swagger。

### Architecture Readiness Assessment

**Overall Status:** READY FOR IMPLEMENTATION

**Confidence Level:** HIGH (历经验证与打磨的业务分层方案)

**Key Strengths:**
- 极大限度降低了安全越权的灾难风险。
- 保护了长连接大屏用户的流畅体验。
- 防范未来的 AI Agent 在开发中产生语言环境错乱。

**Implementation Handoff / AI Agent Guidelines:**
- 各模块专精的 Dev AI Agent 请严格遵循此 Architecture.md 中规定的任何一条命名边界、包结构以及返回值格式。
- 对前端组件只看相关的 Feature 目录。
- 提交 PR 前请自行参考这里的铁律。
