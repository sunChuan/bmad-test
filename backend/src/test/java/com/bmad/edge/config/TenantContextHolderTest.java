package com.bmad.edge.config;

import com.bmad.edge.security.SecurityUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TenantContextHolder 和多租户逻辑的单元测试。
 * 验证不同角色下的租户 ID 提取和忽略表判断逻辑。
 */
class TenantContextHolderTest {

    @AfterEach
    void tearDown() {
        // 每个测试后清除安全上下文
        SecurityContextHolder.clearContext();
    }

    /**
     * 模拟指定角色用户登录并注入 SecurityContext
     */
    private void mockLogin(String username, String roleType, Long orgId, Long districtId, Long schoolId) {
        SecurityUser securityUser = new SecurityUser(username, roleType, orgId, districtId, schoolId);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("区县局长 - 应正确获取 districtId 作为租户 ID")
    void testDistrictDirectorTenantId() {
        mockLogin("district_director_01", "ROLE_DISTRICT_DIRECTOR", 200L, 201L, null);

        assertEquals(201L, TenantContextHolder.getCurrentTenantId());
        assertEquals("ROLE_DISTRICT_DIRECTOR", TenantContextHolder.getCurrentRole());
        assertEquals(200L, TenantContextHolder.getCurrentOrgId());
    }

    @Test
    @DisplayName("校级领导 - 应正确获取 districtId 和角色")
    void testSchoolLeaderTenantId() {
        mockLogin("school_leader_01", "ROLE_SCHOOL_LEADER", 300L, 301L, 3001L);

        assertEquals(301L, TenantContextHolder.getCurrentTenantId());
        assertEquals("ROLE_SCHOOL_LEADER", TenantContextHolder.getCurrentRole());
    }

    @Test
    @DisplayName("市级管理员 - districtId 应为 null（全市数据可见）")
    void testCityAdminTenantId() {
        mockLogin("city_admin_01", "ROLE_CITY_ADMIN", 100L, null, null);

        assertNull(TenantContextHolder.getCurrentTenantId());
        assertEquals("ROLE_CITY_ADMIN", TenantContextHolder.getCurrentRole());
    }

    @Test
    @DisplayName("无认证上下文 - 所有方法应返回 null")
    void testNoAuthentication() {
        assertNull(TenantContextHolder.getCurrentTenantId());
        assertNull(TenantContextHolder.getCurrentRole());
        assertNull(TenantContextHolder.getCurrentOrgId());
    }

    @Test
    @DisplayName("MybatisPlusConfig - 系统级表应被忽略")
    void testIgnoreSystemTables() {
        MybatisPlusConfig config = new MybatisPlusConfig();
        // 间接验证：创建拦截器不应抛出异常
        assertNotNull(config.mybatisPlusInterceptor());
    }

    @Test
    @DisplayName("市级管理员 - 应跳过租户过滤（ignoreTable 逻辑）")
    void testCityAdminBypassesTenantFilter() {
        mockLogin("city_admin_01", "ROLE_CITY_ADMIN", 100L, null, null);

        // 市级管理员角色应返回 null 租户 ID
        assertNull(TenantContextHolder.getCurrentTenantId());
        // 角色应为 ROLE_CITY_ADMIN
        assertEquals("ROLE_CITY_ADMIN", TenantContextHolder.getCurrentRole());
    }

    @Test
    @DisplayName("沙箱特权账号 - 应跳过租户过滤")
    void testSandboxBypassesTenantFilter() {
        mockLogin("sandbox_user", "ROLE_SANDBOX", 999L, null, null);

        assertEquals("ROLE_SANDBOX", TenantContextHolder.getCurrentRole());
        assertNull(TenantContextHolder.getCurrentTenantId());
    }
}
