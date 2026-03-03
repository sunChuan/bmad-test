package com.bmad.edge.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器。
 * 从请求头中解析 JWT，提取角色类型（ROLE_TYPE）和组织/区县/学校 ID，
 * 构建 SecurityUser 并注入到 SecurityContext 中，供后续业务层获取当前用户权限信息。
 * <p>
 * 支持三种角色：
 * - ROLE_CITY_ADMIN：市级超级管理员
 * - ROLE_DISTRICT_DIRECTOR：区县局长
 * - ROLE_SCHOOL_LEADER：校级领导
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/api/mock/") || path.startsWith("/api/v1/auth/sso-callback")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUsernameFromToken(jwt);
                String roleType = jwtUtils.getRoleFromToken(jwt);
                Long orgId = jwtUtils.getOrgIdFromToken(jwt);
                Long districtId = jwtUtils.getDistrictIdFromToken(jwt);
                Long schoolId = jwtUtils.getSchoolIdFromToken(jwt);

                // 验证必要的角色属性是否存在
                if (!StringUtils.hasText(roleType)) {
                    auditLog.warn("[RBAC_MISSING_ROLE] user={}, IP={}, URI={} - JWT 缺少 ROLE_TYPE 属性",
                            username, request.getRemoteAddr(), request.getRequestURI());
                    // 不设置认证，让 AuthEntryPoint 返回 401
                    filterChain.doFilter(request, response);
                    return;
                }

                // 根据角色类型验证必须携带的 ID 属性
                validateRoleAttributes(username, roleType, orgId, districtId, schoolId, request);

                // 构建自定义 SecurityUser 对象并注入 SecurityContext
                SecurityUser securityUser = new SecurityUser(username, roleType, orgId, districtId, schoolId);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("用户认证成功 - user={}, role={}, orgId={}, districtId={}, schoolId={}",
                        username, roleType, orgId, districtId, schoolId);
            }
        } catch (Exception e) {
            auditLog.error("[AUTH_ERROR] IP={}, URI={}, error={}",
                    request.getRemoteAddr(), request.getRequestURI(), e.getMessage());
            log.error("无法设置用户认证: {}", e.getMessage());
            // 不设置认证，让 AuthEntryPoint 处理 401 响应
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 根据角色类型验证必要的 ID 属性，缺失时记录审计日志（警告级别）
     */
    private void validateRoleAttributes(String username, String roleType, Long orgId,
                                        Long districtId, Long schoolId, HttpServletRequest request) {
        switch (roleType) {
            case "ROLE_DISTRICT_DIRECTOR" -> {
                if (districtId == null) {
                    auditLog.warn("[RBAC_INCOMPLETE] user={}, role={}, 缺少 DISTRICT_ID, IP={}, URI={}",
                            username, roleType, request.getRemoteAddr(), request.getRequestURI());
                }
            }
            case "ROLE_SCHOOL_LEADER" -> {
                if (schoolId == null) {
                    auditLog.warn("[RBAC_INCOMPLETE] user={}, role={}, 缺少 SCHOOL_ID, IP={}, URI={}",
                            username, roleType, request.getRemoteAddr(), request.getRequestURI());
                }
                if (districtId == null) {
                    auditLog.warn("[RBAC_INCOMPLETE] user={}, role={}, 缺少 DISTRICT_ID, IP={}, URI={}",
                            username, roleType, request.getRemoteAddr(), request.getRequestURI());
                }
            }
            default -> {
                // ROLE_CITY_ADMIN 等角色无需额外校验
            }
        }
    }

    /**
     * 从 Authorization 请求头中提取 Bearer Token
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
