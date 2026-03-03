---
stepsCompleted: ["step-01-validate-prerequisites", "step-02-design-epics", "step-03-create-stories", "step-04-final-validation"]
inputDocuments: ["prd.md", "architecture.md", "ux-design-specification.md"]
---

# bmad-test - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for bmad-test, decomposing the requirements from the PRD, UX Design if it exists, and Architecture requirements into implementable stories.

## Requirements Inventory

### Functional Requirements

- **FR1:** 用户可以通过政务钉钉或统一 SSO 门户进行免密单点登录。
- **FR2:** 系统可根据登录用户的账号属性，自动赋予市级管理员、区县管理者、校级领导或运维专员的角色。
- **FR3:** 系统可基于用户角色和其绑定的行政区划 ID，对所有业务数据界面进行硬性过滤（行级数据隔离）。
- **FR4:** 系统可定时通过 API 从“教育大数据中心”自动抽取全量历史数据及增量业务数据。
- **FR5:** 运维专员可在一个受限的高级表单内，手工录入或修改由数据中心抽取失败的缺失/错误测评数据。
- **FR5.1:** 运维专员可通过下载标准模板批量导入修正断点/异常数据，系统须在正式入库前提供数据逻辑格式的自动校验与预检报告。
- **FR6:** 系统可自动记录所有手工录入与修改操作的详细审计日志（操作人、时间、修改前/后数值）。
- **FR7:** 具有权限的教研员可在后台“沙箱”界面配置影响增值评价模型（同类分层聚类）的各项指标基准和权重得分。
- **FR8:** 具有权限的教研员可点击“试跑”，在隔离环境中预览按照当前配比权重算出的全市/全区模拟测试排名。
- **FR9:** 试跑结果审核通过后，高权限管理员可将沙箱中的配置方案一键发布，同步覆盖至生产环境算法引擎。
- **FR10:** 市级管理员可查看全覆盖的宏观“区域红绿灯热力图”，直观对比各区县的综合增值态势。
- **FR10.1:** 评价仪表盘在大屏展示各区校排名与指标时，必须显著标识该维度数据的“手工介入/人工修正比例”，提供数据置信度标签及下钻查看修改日志的入口。
- **FR11:** 区县管理者可查看全区雷达图，并向下钻取，展示辖区内每一所学校的“体检明细”和排名变化。
- **FR12:** 校级领导仅能查看本校在各项指标（如学业、身心、师资等）上的长效发展轨迹曲线及雷达画像。
- **FR12.1:** 校级领导在查看本校雷达图与预警时，系统必须提供当前“同类分层”的规则说明页面，以及该层级基线的匿名化统计特征展示（只看同类分位线，不看他校具体数据）。
- **FR13:** 当某项关键指标大幅恶化时，系统可在前端界面亮起显著的“红灯预警”标识。
- **FR14:** 系统可在亮起红灯预警的同时，自动标示出导致该综合指数下降的“最大单一负面影响因子”（如近视率飙升），并且必须同时输出该归因算法的置信度评分（或要求教研员进行人工打标确认），以防范黑盒误判。
- **FR15:** 运维专员或市级管理员可在后台富文本编辑器中录入、分类、修改经验指导文档（对策库）。
- **FR16:** 区县管理者或校级领导在查看指标预警时，系统可展示指向对应问题（如：心理健康对策篇）的静态文档快捷链接文本。
- **FR17:** 系统可通过标准 RESTful 接口向经过认证（Token鉴权）的外部政务平台输出计算完成的区校指标排名。
- **FR18:** API 网关可执行严格的 Rate Limiting 限流策略，拦截恶意的高频轮询调用。

### NonFunctional Requirements

- **NFR1:** 所有存储于数据库中的个体敏感数据必须经过国密算法加密（如 SM4）物理存储。
- **NFR2:** 系统前端或对外 API 展示结果时，不允许反查个源身份明细，需脱敏或匿名聚合输出。
- **NFR3:** 系统建设必须满足国家《信息安全技术网络安全等级保护基本要求》的“第三级”标准。
- **NFR4:** 系统必须对运维的所有数据纠偏及调参动作留存不可篡改的时间戳审计日志。
- **NFR5:** 在市/区级管理者查看宏观热力图并发起下钻切片操作时，前端页面的首屏全量渲染时间不得超过 3秒。
- **NFR6:** 独立增值评价引擎（微服务）在沙箱接收权强调参配置后，处理覆盖 150 所学校规模的全量试算任务，返回全区新排名的计算等待时长不得超过 30分钟。
- **NFR7:** 系统核心业务层（不含跑批沙箱）总体可用性至少需达到 99.9%。
- **NFR8:** 前置“数据中心”拉取任务中断超过 10s需判定超时并启用自动断点续传机制；连续抽取失败 3 次触发独立告警。
- **NFR9:** 面向外部系统开放的脱敏 REST API 接口，在承受 100 QPS 峰值并发时，请求处理的 P95 响应时间必须小于 500ms。
- **NFR10:** 对外开放评估接口时需执行严格兼容的显式版本控制策略（Versioning），以应对自身底层模型算法调参前后的变更。

### Additional Requirements

- **Starter Template (Architecture):** Frontend (Vben Admin v5 + Vue 3 + Tailwind/Ant Design Vue), Backend (Spring Boot 3 + Spring Security + MyBatis-Plus + Java 21), Algorithm (Python).
- **Communication (Architecture):** CQRS pattern. Redis Stream/PubSub for Java-Python async communication. SSE/WebSocket for frontend realtime alerts.
- **Database (Architecture):** MySQL 8 (Shared Database/Shared Schema logical isolation with MyBatis-Plus `TenantLineInnerInterceptor`). Redis JSON/Hash for hot data.
- **Job Orchestration (Architecture):** XXL-JOB or Apache DolphinScheduler for syncing data from Data Center and triggering Python runs.
- **Code Consistency (Architecture):** Strict RESTful APIs (e.g., `/api/v1/assessment/schools/{id}`), `code/message/data` HTTP 200 wrapper, CamelCase for JSON/DTOs, standard `YYYY-MM-DD HH:mm:ss` timestamp format.
- **UX Components:** Requires custom "Focus Pulse Map Node" (ECharts), "AI Diagnostics Accordion" (Drawer + Skeleton loading), and "Knowledge Base Ref Card".
- **UX Interactive Patterns:** No full-page routing for drill-downs (must use Drawers). Critical alerts via Non-dismissible Notifications. Form validations with confirmation modals and inline blur validation.
- **UX Responsive & Accessibility:** Desktop-First design. Mobile devices constrained to a degraded "Alert Feed" view. WCAG 2.1 AA compliant (color-blind safe dual-encoding, >4.5:1 contrast).

### FR Coverage Map

- FR1: Epic 1 - SSO 单点登录接入
- FR2: Epic 1 - RBAC 角色与权限组装
- FR3: Epic 1 - MyBatis-Plus 多租户行级隔离插件
- FR4: Epic 3 - XXL-Job 定时抽数与断点续传
- FR5: Epic 3 - 单条断点数据的强修正表单
- FR5.1: Epic 3 - 模板批量补录与预检校验
- FR6: Epic 3 - 人工介入与防篡改审计日志
- FR7: Epic 3 - 算法指标与权重配置界面
- FR8: Epic 3 - Python 隔离沙箱推演与测算
- FR9: Epic 3 - 试跑审核出库与大基线发布
- FR10: Epic 2 - 市级宏观热力图引擎
- FR10.1: Epic 2 - 图表人工干预率标识与下钻
- FR11: Epic 2 - 区县雷达剖面与学校下钻
- FR12: Epic 2 - 单校时空轨迹图
- FR12.1: Epic 2 - 隐私保密与同类分级展示
- FR13: Epic 2 - WebSocket/SSE 突发红灯告警
- FR14: Epic 2 - 智能归因手风琴与置信度显示
- FR15: Epic 3 - 对策库富文本 CMS 管理
- FR16: Epic 2 - 智库引荐卡片自动关联弹出
- FR17: Epic 4 - 标准 Open API 网关对外画像输出
- FR18: Epic 4 - 高频轮询限流 Rate Limiting

## Epic List

### Epic 1: 安全基座与分级权限接入 (Secure Access & RBAC)
本史诗确立了政务系统的底线，打通市、区、校三级干部的单点登录流程。通过底层 SQL 拦截保障最高级别的数据行级互斥隔离，让用户安全使用系统。
**FRs covered:** FR1, FR2, FR3

#### Story 1.1: 基础工程架构初始化与脚手架搭建
**As a** 系统开发/运维人员,
**I want** 基于 Vben Admin v5 构建前端基座，并基于 Spring Boot 3 搭建后端无状态网关（包含 MySQL与Redis连接池）,
**So that** 后续所有的界面开发和 API 编写都拥有统一的、符合等保规范的基础骨架和拦截器生命周期。

**Acceptance Criteria:**
- **Given** 裸机/私有云开发环境
- **When** 开发人员执行前后端 Start 命令
- **Then** 前端 Vben5 默认登录页正常渲染，后端 Spring Boot Actuator `/health` 接口返回 UP
- **And** 前端 Axios 配置中已强制约束接收 `{code: 200, data, message}` 的政务标准 JSON 格式。

#### Story 1.2: 统一身份鉴权网关对接 (SSO & JWT)
**As a** 市局或区县真实用户,
**I want** 直接使用政务钉钉/统一 SSO 门户扫码或免密跳转登录大屏,
**So that** 我不需要记忆额外的账号密码，且登录过程符合政务内网安全审计要求 (FR1)。

**Acceptance Criteria:**
- **Given** 用户未携带有效 Token 访问私有系统 URL
- **When** 系统路由守卫捕获请求
- **Then** 自动重定向至 SSO 中心，SSO 回调成功后，Spring Security 根据返回的 Auth Code 签发带有超期时间的自主 JWT Token。
- **And** 用户端只能看到经过脱敏的 Token，无法反向解析出密码明文 (NFR2)。

#### Story 1.3: 用户角色与政务权限打标 (RBAC Core)
**As a** 系统安全管理员,
**I want** 在用户登录时，Spring Security 能准确识别它是“市级超级管理员”、“区县局长”还是“校级领导”并提取其对应的 `district_id` 或 `school_id`,
**So that** 前端 Vben Admin 能够根据这些标识动态隐藏或显示不同的菜单和路由 (FR2)。

**Acceptance Criteria:**
- **Given** 已从 SSO 获取身份并进入 Java 层过滤器
- **When** Security Context 解析 JWT Payload
- **Then** 必须至少提取出 `ROLE_TYPE` 和 `ORG_ID` 两个关键 Claim 注入进会话。
- **And** 若遇到角色属性缺失的异常账号，立即抛出 Http 401 并记录审计日志。

#### Story 1.4: MyBatis-Plus 逻辑多租户防御基座 (Tenant Interceptor)
**As a** 数据安全合规审核员,
**I want** 后端数据库框架能够“透明且强制”地在所有 `SELECT`, `UPDATE`, `DELETE` 语句尾部拼装当前用户的 `ORG_ID` 过滤条件,
**So that** 无论前端 API 怎样请求，区县 A 的局长在物理层面绝对不可能查出区县 B 的任何办学数据 (FR3, NFR3)。

**Acceptance Criteria:**
- **Given** 一名合法的区县级领导发起 `获取所有预警学校` 的 API 请求
- **When** Java `DashboardService` 执行无条件的 `schoolMapper.selectList(null)`
- **Then** 控制台打印的真实 SQL 必须被防腐层拦截器改写为 `WHERE district_id = '他自己的区ID' AND is_deleted = 0`。
- **And** 这个拦截机制必须对除特权沙箱账以外的所有业务请求全局生效，不可绕过。

### Epic 2: 大屏指挥舱与异常雷达 (Dashboard & Diagnosis Radar)
系统“脸面”与决策核心。为管理者呈现从宏观区域到具体某个指标波动的全维下钻体验。利用定制的聚光节点和抽屉诊断面板快速捕捉危机并推介经验。
**FRs covered:** FR10, FR10.1, FR11, FR12, FR12.1, FR13, FR14, FR16

#### Story 2.1: 初始化市区级宏观热力图缓存网关
**As a** 市级管理员,
**I want** 登录主页后能瞬间 (<3秒) 看到全市 11 个区县的增值评价雷达图和总体分数流向图,
**So that** 能够对总体教育生态有一个立刻的宏观把握 (FR10, NFR5)。

**Acceptance Criteria:**
- **Given** MySQL 中有大量的历年成绩且计算过程极耗时
- **When** 前端页面初始化 `/api/v1/dashboard/macro/heat-map`
- **Then** Java API 网关直接命中 Redis 中的前置聚类 JSON 缓存，并在 500ms 内返回数据。
- **And** 数据中包含一个标志位，用于在前端提示该数据的最后刷新时间以及人工修正数据的占比率 (FR10.1)。

#### Story 2.2: ECharts 聚光异常图谱节点封装 (定制组件 1)
**As a** 区县教育局长,
**I want** 在大屏中间看到我的辖区内各所学校呈现为“体积不等”的 Treemap色块（或散点），当有学校告警时，色块会自动变成红色脉冲（Pulse）状,
**So that** 我能在眼花缭乱的数据大屏中第一秒就锁定出问题的学校。

**Acceptance Criteria:**
- **Given** 获得了由 API 下发的带有状态标的学校列表树
- **When** 此前端组件挂载并在 ECharts 渲染该图谱
- **Then** 高危节点呈暗红色，并带有 CSS3 / SVG 微弱脉冲动效。
- **And** 当我对某个节点执行 `Click` 或 `Hover` 时，周围节点自动应用 `opacity: 0.3` 加遮罩暗化，突出显示当前学校节点。

#### Story 2.3: WebSocket/SSE 突发红标实时告警
**As a** 在线监控的教育管理者,
**I want** 在不刷新页面的情况下，一旦后台有新增的确诊劣势指标，系统能立刻在右上角弹出不可隐藏的“强通知框 (Notification)”,
**So that** 我对断崖式波动的感知甚至早于学期结束 (FR13)。

**Acceptance Criteria:**
- **Given** 管理者正停留在 Dashboard 页面
- **When** 后台 Python 将某校增值指标判定为“严重下降”并推入 Java 队列
- **Then** 用户的浏览器通过 SSE/WebSocket 接收到警告流指令。
- **And** 显示的 Notification 框 `Duration` 必须强制为 `0` (不可超时自动关闭)，必须由用户点击“查看抽屉详情”或“暂不处理”来显式结束。

#### Story 2.4: 智能诊断抽屉与归因手风琴 (定制组件 2)
**As a** 关注预警的区域负责人,
**I want** 点击某个报警学校后，页面右侧滑出一个抽屉 (Drawer)，利用智能“手风琴”层层揭示：是哪项单项指标（如：师资流失率激增）导致了总体排名的恶化，并且附上模型的置信度评分,
**So that** 我无需离开当前上下文页面跳转，就能精准诊断原因而不遭受“黑盒误判” (FR11, FR14)。

**Acceptance Criteria:**
- **Given** 抽屉滑出时获取底层因素溯源的 API 存在 1-3 秒的请求延迟
- **When** 抽屉正在请求溯源详情时
- **Then** 必须呈现精致的骨架屏 (Skeleton) 进行过渡。
- **And** 当数据返回时，手风琴头部 (Header) 直接打印红色的归因结果文本（“最大剥离因子为：XXX”），并在下方配以支持性 ECharts 走势明细和算法置信度 (>=0~100%)。

#### Story 2.5: 静态经验智库图文引荐卡 (定制组件 3)
**As a** 面临困境的管理者,
**I want** 在诊断抽屉的最下方，能看到一两张“高亮引荐”卡片，它们链接到以往解决类似突发指标异常的内部经验红头文件,
**So that** 系统不仅能在“破窗”时报警，还能立刻递上“修窗工具”，缩短我的决策迷茫期 (FR16)。

**Acceptance Criteria:**
- **Given** API 侧已经通过“异常标签匹配”（例如匹配了 `tag:师资补漏`）给出了相关的智库文献 ID 分组
- **When** 前端诊断抽屉完全渲染结束
- **Then** 抽屉底部区域渲染 `Ref Card` 组件。
- **And** 如果匹配度（权重）极高，卡片左上角叠加“✨ AI 强推”徽章；点击卡片在右侧展开侧边预览大文档 (而非跳转离开页面)。

### Epic 3: 归因沙箱与底库防线 (Algorithm Sandbox & Data Fallback)
教研专家和运维专员的硬核操作台。承载整个增值评价模型算法体系的沙盒调参验证、失败数据的高级干预补救与经验库的上传下发。
**FRs covered:** FR4, FR5, FR5.1, FR6, FR7, FR8, FR9, FR15

#### Story 3.1: XXL-JOB 定时跑批与断点续传管线
**As a** 系统运维专员,
**I want** 系统能够依靠独立调度中心长连接前置的大数据局，拉取学期测验数据，并在断网时具有 3 次重试及断点续传能力,
**So that** 我不需要每天手工同步数据，同时能保障核心业务库数据的绝对完整 (FR4, NFR8)。

**Acceptance Criteria:**
- **Given** XXL-JOB 调度中心已连接至 Spring Boot 执行器
- **When** 触发每日凌晨 2 点的 `DataSyncJob`
- **Then** 系统以批处理方式拉取外部 API 并平滑 Upsert 到 MySQL 基础表中。
- **And** 若中途遇 HTTP 500 或 Timeout 超过 10s，任务必须标记暂停并进入队列重试，连续 3 次失败则触发钉钉/短信级最高告警，等待运维介入。

#### Story 3.2: 强时效的人工断点补录与防篡改审计
**As a** 运维专员或数据清洗员,
**I want** 在后台以受限的高级表单（单条添加）或 Excel 模板（批量导入）的方式，强行录入或覆盖大屏所需的底库缺失数据，且一切操作不可逆留痕,
**So that** 前端大屏不会因为前置数据的脏活而导致“图表挂掉”，同时操作能经得起随时追查 (FR5, FR5.1, FR6, NFR4)。

**Acceptance Criteria:**
- **Given** 运维人员进入“成绩底座维护”路由
- **When** 发起批量 Excel 上传或提交单条补录表单
- **Then** 数据保存入库前必须经历一次 Java 层面的逻辑校验（如：分数不可 > 150，空置率不可 > 10%），如果不满足则拦截并高亮阻断原因。
- **And** 通过校验入库的动作，必须通过 AOP 或 MyBatis Interceptor 同步双写一条不可 `UPDATE`/`DELETE` 的审计日志记录至 `edu_audit_log`（须包含修改前原值、新值、操作人及时间）。

#### Story 3.3: 算法参数配置面板与沙箱通信器
**As a** 市级教研员,
**I want** 在一个沙箱视图里滑动修改各项基础指标（如德育、智育比例）对最终聚类模型的“主干权重评分”,
**So that** 我能够自由探索让某些弱势学校脱颖而出的评价模式 (FR7)。

**Acceptance Criteria:**
- **Given** 教研员调整好了表单内的数十根 Slider 参数
- **When** 点击下方的【锁定参数并装入沙箱】按钮
- **Then** Vben 后台通过 `/api/v1/sandbox/config` 发送 JSON 给 Java，Java 验证权限后立刻将其丢进 Redis 的等待队列（Queue），并反馈 "Config Queued" 状态给前端。

#### Story 3.4: 异步“试跑”调度与进度长轮询
**As a** 正在做研究的市级教研员,
**I want** 点击【执行全区模拟试跑】后，能看到 Python 集群开始接管任务，并有一个明确的加载进度条，告知我大约几分钟能出模拟排名图,
**So that** 我在大规模（长达 30 分钟）的算力运行期不会认为系统假死 (FR8, NFR6)。

**Acceptance Criteria:**
- **Given** 沙箱参数已被装载进内存
- **When** 接口触发试跑指令
- **Then** Python Engine 从队列中摘除任务启动子进程，并在每完成 5 所学校的模拟评卷时，就往 Redis 更新一条 `Progress: %` 状态字。
- **And** 前端界面通过长轮询（或 SSE）实时反馈这个百分比进度条；计算完成后，渲染一张仅供该教研员可见的“沙盘全景雷达图”。

#### Story 3.5: 沙箱出库审批与大基线发布更迭
**As a** 市级行政长官 (超级管理员),
**I want** 在审阅完教研员提审的某次“沙箱模拟数据”极其亮眼时，点击【一键部署至大屏主线】,
**So that** 当前暂存的模型权重配置，能瞬间覆盖生产环境，成为本学期的主基准线 (FR9)。

**Acceptance Criteria:**
- **Given** 沙箱中有一条状态为 `WAIT_PUBLISH` 的模拟结果
- **When** 管理员点击发布
- **Then** 该套配置的元数据版本号 +1，Java 网关将 Redis 热点缓存彻底按这套新基线重置，旧缓存作为历史封存。
- **And** 此时前端大屏全体在下一刻请求时将默认引用最新的模型打分排位。

#### Story 3.6: 经验对策库 CMS 富文本管理
**As a** 运维组员,
**I want** 一个简单类似博客后台的界面，允许我粘贴带格式的图文、标上分类（如：心理疏导类、师资调配类），并发表至“内部智库”,
**So that** 在 Epic 2 中的大屏报错时，系统有东西可以抽调并推荐出来 (FR15)。

**Acceptance Criteria:**
- **Given** 用户拥有该知识库的写权限
- **When** 使用自带的 TinyMCE 或等效富文本组件编辑文章并加上 Tag
- **Then** 可以顺利将带 HTML 标签的文章实体保存入库并形成唯一 Article ID。

### Epic 4: 开放协同中台 (Open API Integration)
打造数据的下游出口，对其他平级的政务应用进行脱敏画像共享，并具备防御恶意的并发调用能力。
**FRs covered:** FR17, FR18

#### Story 4.1: 标准 Open API 网关对外画像输出
**As a** 外部政务系统开发者,
**I want** 能够携带系统派发的 Open-Token 访问特定的 RESTful API 接口，直接获取本区县最新的脱敏雷达数据,
**So that** 我可以将这些洞察集成到我们自有的教育小程序中 (FR17, NFR2)。

**Acceptance Criteria:**
- **Given** 外部系统已申请了带有 `SCOPE_OPEN_API` 的 API-Key
- **When** 外部系统调用版本化的接口，如 `GET /open-api/v1/assessment/schools/{id}`
- **Then** 接口必须在 500ms(P95) 内响应 (NFR9)。
- **And** 响应的数据包中绝对不能包含学生真实姓名、身份证等物理主键或明文信息，彻底匿名聚合化 (NFR2, NFR10)。

#### Story 4.2: 高频轮询拦截限流卫士 (Rate Limiting)
**As a** 系统维稳人员,
**I want** 系统在 Open API 的入口处部署坚固的滑动窗口限流器,
**So that** 即使外部小程序发生 bug 发起疯狂轮询，也不会把我们这边的 Redis 缓存或者 Java 内存打爆 (FR18)。

**Acceptance Criteria:**
- **Given** 开放 API 已部署并配置了“每个 API-Key 最高 100 QPS” 的规则
- **When** 某个不良外部客户端以 500 QPS 的速度并发请求
- **Then** 前 100 个请求正常以 HTTP 200 返回。
- **And** 第 101 个至 500 个请求必须直接在 Gateway / Filter 层被拦截，并立即返回 HTTP 429 (Too Many Requests) 且不穿透到下层业务逻辑。
