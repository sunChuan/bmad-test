package com.bmad.edge.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 工具类，负责令牌的生成、解析与验证。
 * 支持 ROLE_TYPE、ORG_ID、DISTRICT_ID、SCHOOL_ID 等政务角色属性。
 */
@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret:QWEASDZXC1234567890qwertyuiopasdfghjklzxcvbnm_DEFAULT_SECRET}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private int jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * 生成 JWT Token，包含角色类型与组织/区县/学校 ID
     */
    public String generateToken(String username, String role, Long orgId, Long districtId, Long schoolId) {
        var builder = Jwts.builder()
                .subject(username)
                .claim("ROLE_TYPE", role)
                .claim("ORG_ID", orgId)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs));

        // 仅当有值时才写入对应 claim，减少 payload 体积
        if (districtId != null) {
            builder.claim("DISTRICT_ID", districtId);
        }
        if (schoolId != null) {
            builder.claim("SCHOOL_ID", schoolId);
        }

        return builder.signWith(getSigningKey()).compact();
    }

    /**
     * 保留兼容的旧版生成方法（orgId 模式）
     */
    public String generateToken(String username, String role, Long orgId) {
        return generateToken(username, role, orgId, null, null);
    }

    /**
     * 从 JWT 中解析所有 Claims
     */
    public Claims parseAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsernameFromToken(String token) {
        return parseAllClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return parseAllClaims(token).get("ROLE_TYPE", String.class);
    }

    public Long getOrgIdFromToken(String token) {
        return parseAllClaims(token).get("ORG_ID", Long.class);
    }

    public Long getDistrictIdFromToken(String token) {
        return parseAllClaims(token).get("DISTRICT_ID", Long.class);
    }

    public Long getSchoolIdFromToken(String token) {
        return parseAllClaims(token).get("SCHOOL_ID", Long.class);
    }

    /**
     * 验证 JWT Token 有效性
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (Exception e) {
            log.warn("JWT 验证失败: {}", e.getMessage());
        }
        return false;
    }
}
