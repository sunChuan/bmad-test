# Story 1.4: MyBatis-Plus 逻辑多租户防御基座 (Tenant Interceptor)

Status: review

## Story

**As a** 数据安全合规审核员,
**I want** 后端数据库框架能够"透明且强制"地在所有 `SELECT`, `UPDATE`, `DELETE` 语句尾部拼装当前用户的 `ORG_ID` 过滤条件,
**So that** 无论前端 API 怎样请求，区县 A 的局长在物理层面绝对不可能查出区县 B 的任何办学数据 (FR3, NFR3)。

## Acceptance Criteria

1. **Given** 一名合法的区县级领导发起 `获取所有预警学校` 的 API 请求
2. **When** Java `DashboardService` 执行无条件的 `schoolMapper.selectList(null)`
3. **Then** 控制台打印的真实 SQL 必须被防腐层拦截器改写为 `WHERE district_id = '他自己的区ID' AND is_deleted = 0`。
4. **And** 这个拦截机制必须对除特权沙箱账号以外的所有业务请求全局生效，不可绕过。

## Tasks / Subtasks

- [x] Task 1: 配置 MyBatis-Plus TenantLineInnerInterceptor
  - [x] 在 MyBatis-Plus 配置类中注册 `TenantLineInnerInterceptor`
  - [x] 实现自定义 `TenantLineHandler`，从 `SecurityContextHolder` 中的 `SecurityUser` 获取当前用户的 `districtId`
  - [x] 配置租户字段名（`district_id`）和需要忽略拦截的系统级表列表
- [x] Task 2: 市级管理员特权放行机制
  - [x] `ROLE_CITY_ADMIN` 角色跳过租户过滤（全市数据可见）
  - [x] 沙箱特权账号（`ROLE_SANDBOX`）跳过租户过滤
- [x] Task 3: 演示用 Mapper 与集成测试
  - [x] 创建示例 Entity（`School`）和对应的 `SchoolMapper`
  - [x] 编写单元测试验证不同角色下的租户 ID 提取
  - [x] 验证 ROLE_CITY_ADMIN 和 ROLE_SANDBOX 不附加租户条件

## Dev Agent Record

### Completion Notes List
- 创建 `MybatisPlusConfig.java` - TenantLineInnerInterceptor 注册 + ignoreTable 逻辑（系统表/市级管理员/沙箱跳过）
- 创建 `TenantContextHolder.java` - 从 SecurityContext 提取 districtId/roleType，避免 ThreadLocal 开销
- 创建 `School.java` - 示例实体，含 district_id 租户字段和 @TableLogic 逻辑删除
- 创建 `SchoolMapper.java` - 继承 BaseMapper，自动受租户拦截器保护
- 创建 `TenantContextHolderTest.java` - 7 个测试覆盖区县/校级/市级/沙箱/无认证场景
- 更新 `application.yml` - 添加 MyBatis-Plus SQL 日志、驼峰映射、逻辑删除配置

### File List
- `backend/src/main/java/com/bmad/edge/config/MybatisPlusConfig.java` [NEW]
- `backend/src/main/java/com/bmad/edge/config/TenantContextHolder.java` [NEW]
- `backend/src/main/java/com/bmad/edge/entity/School.java` [NEW]
- `backend/src/main/java/com/bmad/edge/repository/SchoolMapper.java` [NEW]
- `backend/src/main/resources/application.yml` [MODIFY]
- `backend/src/test/java/com/bmad/edge/config/TenantContextHolderTest.java` [NEW]
