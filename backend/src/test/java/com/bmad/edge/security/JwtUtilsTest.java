package com.bmad.edge.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtils 与 SecurityUser 单元测试。
 * 覆盖正常与异常场景下的 JWT 生成、解析、角色属性提取。
 */
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        // 注入测试用密钥（满足 HMAC-SHA256 最低 32 字节要求）
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret",
                "TestSecretKey1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 86400000);
    }

    @Test
    @DisplayName("市级管理员 - 生成与解析 Token 应正确提取角色和组织ID")
    void testCityAdminTokenGeneration() {
        String token = jwtUtils.generateToken("city_admin", "ROLE_CITY_ADMIN", 100L, null, null);

        assertNotNull(token);
        assertEquals("city_admin", jwtUtils.getUsernameFromToken(token));
        assertEquals("ROLE_CITY_ADMIN", jwtUtils.getRoleFromToken(token));
        assertEquals(100L, jwtUtils.getOrgIdFromToken(token));
        assertNull(jwtUtils.getDistrictIdFromToken(token));
        assertNull(jwtUtils.getSchoolIdFromToken(token));
    }

    @Test
    @DisplayName("区县局长 - Token 应正确包含 DISTRICT_ID")
    void testDistrictDirectorTokenGeneration() {
        String token = jwtUtils.generateToken("district_director", "ROLE_DISTRICT_DIRECTOR", 200L, 201L, null);

        assertEquals("ROLE_DISTRICT_DIRECTOR", jwtUtils.getRoleFromToken(token));
        assertEquals(200L, jwtUtils.getOrgIdFromToken(token));
        assertEquals(201L, jwtUtils.getDistrictIdFromToken(token));
        assertNull(jwtUtils.getSchoolIdFromToken(token));
    }

    @Test
    @DisplayName("校级领导 - Token 应正确包含 DISTRICT_ID 和 SCHOOL_ID")
    void testSchoolLeaderTokenGeneration() {
        String token = jwtUtils.generateToken("school_leader", "ROLE_SCHOOL_LEADER", 300L, 301L, 3001L);

        assertEquals("ROLE_SCHOOL_LEADER", jwtUtils.getRoleFromToken(token));
        assertEquals(301L, jwtUtils.getDistrictIdFromToken(token));
        assertEquals(3001L, jwtUtils.getSchoolIdFromToken(token));
    }

    @Test
    @DisplayName("无效 Token 验证应返回 false")
    void testInvalidTokenValidation() {
        assertFalse(jwtUtils.validateJwtToken("invalid.token.here"));
        assertFalse(jwtUtils.validateJwtToken(""));
        assertFalse(jwtUtils.validateJwtToken(null));
    }

    @Test
    @DisplayName("合法 Token 验证应返回 true")
    void testValidTokenValidation() {
        String token = jwtUtils.generateToken("user", "ROLE_CITY_ADMIN", 1L);
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    @DisplayName("SecurityUser 应正确封装角色与组织属性")
    void testSecurityUserCreation() {
        SecurityUser user = new SecurityUser("test_user", "ROLE_SCHOOL_LEADER", 300L, 301L, 3001L);

        assertEquals("test_user", user.getUsername());
        assertEquals("ROLE_SCHOOL_LEADER", user.getRoleType());
        assertEquals(300L, user.getOrgId());
        assertEquals(301L, user.getDistrictId());
        assertEquals(3001L, user.getSchoolId());
        assertNull(user.getPassword());
        assertTrue(user.isEnabled());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertEquals(1, user.getAuthorities().size());
    }

    @Test
    @DisplayName("SecurityUser 角色为 null 时应默认为 ROLE_USER")
    void testSecurityUserWithNullRole() {
        SecurityUser user = new SecurityUser("test_user", null, 1L, null, null);
        assertEquals("ROLE_USER", user.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    @DisplayName("兼容旧版 generateToken 方法（三参数）")
    void testLegacyGenerateToken() {
        String token = jwtUtils.generateToken("admin", "ROLE_CITY_ADMIN", 1L);

        assertNotNull(token);
        assertEquals("admin", jwtUtils.getUsernameFromToken(token));
        assertEquals("ROLE_CITY_ADMIN", jwtUtils.getRoleFromToken(token));
        assertNull(jwtUtils.getDistrictIdFromToken(token));
    }

    @Test
    @DisplayName("parseAllClaims 应返回完整的 Claims 对象")
    void testParseAllClaims() {
        String token = jwtUtils.generateToken("user1", "ROLE_DISTRICT_DIRECTOR", 200L, 201L, null);
        Claims claims = jwtUtils.parseAllClaims(token);

        assertNotNull(claims);
        assertEquals("user1", claims.getSubject());
        assertEquals("ROLE_DISTRICT_DIRECTOR", claims.get("ROLE_TYPE", String.class));
        assertEquals(201L, claims.get("DISTRICT_ID", Long.class));
    }
}
