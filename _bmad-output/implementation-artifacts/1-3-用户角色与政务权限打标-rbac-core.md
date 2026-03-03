# Story 1.3: 用户角色与政务权限打标 (RBAC Core)

Status: review

## Story

**As a** 系统安全管理员,
**I want** 在用户登录时，Spring Security 能准确识别它是"市级超级管理员"、"区县局长"还是"校级领导"并提取其对应的 `district_id` 或 `school_id`,
**So that** 前端 Vben Admin 能够根据这些标识动态隐藏或显示不同的菜单和路由 (FR2)。

## Acceptance Criteria

1. **Given** 已从 SSO 获取身份并进入 Java 层过滤器
2. **When** Security Context 解析 JWT Payload
3. **Then** 必须至少提取出 `ROLE_TYPE` 和 `ORG_ID` 两个关键 Claim 注入进会话。
4. **And** 若遇到角色属性缺失的异常账号，立即抛出 Http 401 并记录审计日志。

## Tasks / Subtasks

- [x] Task 1: 完善后端 Spring Security Token 角色解析与注入
  - [x] 修改 `JwtAuthenticationFilter` 以便从 JWT 中解析出带有 `role` 和 `orgId` 权限的声明
  - [x] 基于解析出的内容构建自定义的 `UserDetails` 对象（`SecurityUser`），存储相应的 `orgId` 和 `GrantedAuthority` 列表
  - [x] 将封装好的 Authentication 对象存入 `SecurityContextHolder` 供全局共享
- [x] Task 2: JWT 无效或异常处理
  - [x] 当 JWT 解析失败，或缺失必要的角色属性时，直接记录异常 Audit Log 并向前端返回 401 状态码 (统一异常封装)
- [x] Task 3: 联调与集成测试
  - [x] 编写单元测试确认正常与非法 JWT 下的上下文注入和阻断表现

## Dev Notes

- **Relevant architecture patterns and constrains**: 
  - Backend:基于 Spring Boot 3 + Java 17 + Spring Security 进行核心过滤器拓展。异常返回遵循统一的 JSON Wrapper。
- **Source tree components to touch**:
  - `backend/src/main/java/com/bmad/edge/security/JwtAuthenticationFilter.java` [MODIFY]
  - `backend/src/main/java/com/bmad/edge/security/SecurityUser.java` [NEW]
  - `backend/src/main/java/com/bmad/edge/security/JwtAuthEntryPoint.java` [NEW]

## Dev Agent Record

### Completion Notes List
- 创建 `SecurityUser.java` - 自定义 UserDetails，封装 roleType/orgId/districtId/schoolId
- 创建 `JwtAuthEntryPoint.java` - 认证失败时返回统一 JSON 401 + 审计日志
- 增强 `JwtUtils.java` - 支持 DISTRICT_ID/SCHOOL_ID claim, parseAllClaims 公共方法
- 重构 `JwtAuthenticationFilter.java` - SecurityUser 注入, 角色属性校验, 审计日志
- 更新 `SecurityConfig.java` - 集成 JwtAuthEntryPoint
- 增强 `AuthController.java` - 三种角色 Mock SSO (city_/district_/school_ 前缀)
- 新增 `JwtUtilsTest.java` - 9 个单元测试全部通过

### File List
- `backend/src/main/java/com/bmad/edge/security/SecurityUser.java` [NEW]
- `backend/src/main/java/com/bmad/edge/security/JwtAuthEntryPoint.java` [NEW]
- `backend/src/main/java/com/bmad/edge/security/JwtUtils.java` [MODIFY]
- `backend/src/main/java/com/bmad/edge/security/JwtAuthenticationFilter.java` [MODIFY]
- `backend/src/main/java/com/bmad/edge/security/SecurityConfig.java` [MODIFY]
- `backend/src/main/java/com/bmad/edge/controller/AuthController.java` [MODIFY]
- `backend/src/test/java/com/bmad/edge/security/JwtUtilsTest.java` [NEW]
