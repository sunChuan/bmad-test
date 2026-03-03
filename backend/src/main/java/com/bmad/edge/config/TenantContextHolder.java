package com.bmad.edge.config;

import com.bmad.edge.security.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 租户上下文持有者。
 * 从 SecurityContextHolder 中的 SecurityUser 提取当前用户的租户信息（districtId、roleType）。
 * 供 MyBatis-Plus TenantLineHandler 在 SQL 拦截时使用。
 * 
 * 设计说明：
 * - 不使用 ThreadLocal 手动管理，而是直接从 Spring Security 上下文读取
 * - 保证租户信息的单一数据源（JWT -> SecurityUser -> TenantContextHolder）
 */
public final class TenantContextHolder {

    private TenantContextHolder() {
        // 工具类不允许实例化
    }

    /**
     * 获取当前用户的租户 ID（即 districtId）。
     * 
     * @return 区县 ID，若无认证上下文则返回 null
     */
    public static Long getCurrentTenantId() {
        SecurityUser user = getCurrentSecurityUser();
        return user != null ? user.getDistrictId() : null;
    }

    /**
     * 获取当前用户的角色类型。
     * 
     * @return 角色类型字符串（如 ROLE_CITY_ADMIN），若无认证上下文则返回 null
     */
    public static String getCurrentRole() {
        SecurityUser user = getCurrentSecurityUser();
        return user != null ? user.getRoleType() : null;
    }

    /**
     * 获取当前用户的组织 ID。
     * 
     * @return 组织 ID，若无认证上下文则返回 null
     */
    public static Long getCurrentOrgId() {
        SecurityUser user = getCurrentSecurityUser();
        return user != null ? user.getOrgId() : null;
    }

    /**
     * 从 SecurityContext 中提取 SecurityUser 对象。
     * 
     * @return SecurityUser 实例，若不存在或类型不匹配则返回 null
     */
    private static SecurityUser getCurrentSecurityUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            return (SecurityUser) authentication.getPrincipal();
        }
        return null;
    }
}
