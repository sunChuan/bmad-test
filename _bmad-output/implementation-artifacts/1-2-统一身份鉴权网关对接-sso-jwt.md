# Story 1.2: 统一身份鉴权网关对接 (SSO & JWT)

Status: review

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

**As a** 市局或区县真实用户,
**I want** 直接使用政务钉钉/统一 SSO 门户扫码或免密跳转登录大屏,
**So that** 我不需要记忆额外的账号密码，且登录过程符合政务内网安全审计要求 (FR1)。

## Acceptance Criteria

1. **Given** 用户未携带有效 Token 访问私有系统 URL
2. **When** 前端系统路由守卫捕获请求
3. **Then** 自动重定向至 SSO 中心，SSO 回调成功后，Spring Security 根据返回的 Auth Code 签发带有超期时间的自主 JWT Token。
4. **And** 用户端只能看到经过脱敏的 Token，无法反向解析出密码明文 (NFR2)。

## Tasks / Subtasks

- [x] Task 1: 完善后端 Spring Security & JWT 鉴权体系
  - [x] 引入并配置 JWT 工具类 (生成、解析、校验 Token)
  - [x] 编写 SSO 模拟回调接口 `/api/v1/auth/sso-callback`（接收 Code 并发放 JWT）
  - [x] 编写 JWT 过滤器 `JwtAuthenticationFilter` 并注册到 Spring Security
  - [x] 配置 `SecurityConfig` 放行登录回调接口，拦截其他业务接口
- [x] Task 2: 完善前端 Vben Admin 路由鉴权与 SSO 对接
  - [x] 在 `frontend/src/api/` 下定义统一的 Axios Auth 拦截器 (携带 `Authorization: Bearer <token>`)
  - [x] 编写前端 Vue Router 路由守卫：检测本地无 Token 且访问受保护路由时，跳转到模拟的 SSO 授权页 [FR1]
  - [x] 编写处理 SSO 回调的前端页面/组件，接收 Token 并存入 `localStorage` 或状态管理中，随后跳转至首页
- [x] Task 3: 联调与集成测试
  - [x] 确保前端无 Token 访问 `/api/v1/system/version` 返回 401
  - [x] 确保完成拦截重定向和 Token 解析签发的全流程跑通

## Dev Notes

- **Relevant architecture patterns and constrains**: 
  - Backend:基于 Spring Boot 3 + Java 21 + Spring Security 进行开发。API 返回格式必须遵循规范包装类 `{code, data, message}`。
  - Frontend: 基于现有 Vite + Vue3 基础工程。由于前端使用了暂时的精简模版而非完整 Vben5 面板，在编写路由守卫时，直接操作 `vue-router` 的 `beforeEach` 即可。 
- **Source tree components to touch**:
  - `backend/src/main/java/com/bmad/edge/security/JwtUtils.java` [NEW]
  - `backend/src/main/java/com/bmad/edge/security/JwtAuthenticationFilter.java` [NEW]
  - `backend/src/main/java/com/bmad/edge/security/SecurityConfig.java` [MODIFY]
  - `backend/src/main/java/com/bmad/edge/controller/AuthController.java` [NEW]
  - `frontend/src/main.ts` (or router setup) [MODIFY]
  - `frontend/src/api/index.ts` [MODIFY]
- **Testing standards summary**: 
  - 添加针对 `/api/v1/auth/sso-callback` 和保护接口的自动化基础单元测试（如有条件）。

### Project Structure Notes

- Alignment with unified project structure: 后端代码都在 `backend/` 下的 `com.bmad.edge.*` 包中，需确保 `security` 模块结构清晰。前端代码在 `frontend/src` 内部维护 API 和路由（`router/index.ts` 需要自行创建并挂载）。

### References

- [Source: `_bmad-output/planning-artifacts/epics.md#Epic 1: 安全基座与分级权限接入`]

## Dev Agent Record

### Agent Model Used

Antigravity (code-agent)

### Debug Log References

### Completion Notes List
- 引入了 io.jsonwebtoken 以适配 Spring Boot 3 + Java 17/21 环境生成和解析 JWT
- 自定义了 JwtAuthenticationFilter 进行无状态 Token 验证与鉴权拦截
- 配置了 SecurityConfig 使其忽略 Session 创建，并允许跨域 SSO 登录回调 API 访问
- 编写了 AuthController 用于接受前台换签 OAuth code 以派发 Token
- 在 Vue 前端中补充了 Axios 请求拦截器注入 Bearer Auth Token
- 编写了 vue-router 的拦截逻辑保护并引导未登录访问者前往 /login SSO 模拟页
- 分别完成 SSO 交互页面和业务页面数据渲染逻辑联调

### File List
- `backend/pom.xml` [MODIFY]
- `backend/src/main/java/com/bmad/edge/security/JwtUtils.java` [NEW]
- `backend/src/main/java/com/bmad/edge/security/JwtAuthenticationFilter.java` [NEW]
- `backend/src/main/java/com/bmad/edge/security/SecurityConfig.java` [MODIFY]
- `backend/src/main/java/com/bmad/edge/controller/AuthController.java` [NEW]
- `frontend/src/api/index.ts` [MODIFY]
- `frontend/src/router/index.ts` [NEW]
- `frontend/src/views/Login.vue` [NEW]
- `frontend/src/views/Home.vue` [NEW]
- `frontend/src/main.ts` [MODIFY]
- `frontend/src/App.vue` [MODIFY]
