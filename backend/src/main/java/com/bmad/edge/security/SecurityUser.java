package com.bmad.edge.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 自定义安全用户对象，封装 JWT 中解析出的角色信息与所属组织 ID。
 * 支持三种核心角色：ROLE_CITY_ADMIN（市级超级管理员）、ROLE_DISTRICT_DIRECTOR（区县局长）、ROLE_SCHOOL_LEADER（校级领导）。
 */
@Getter
public class SecurityUser implements UserDetails {

    /** 用户名（JWT subject） */
    private final String username;

    /** 角色类型，如 ROLE_CITY_ADMIN / ROLE_DISTRICT_DIRECTOR / ROLE_SCHOOL_LEADER */
    private final String roleType;

    /** 所属组织 ID（区县 ID 或学校 ID，视角色类型而定） */
    private final Long orgId;

    /** 区县 ID（仅 ROLE_DISTRICT_DIRECTOR / ROLE_SCHOOL_LEADER 有值） */
    private final Long districtId;

    /** 学校 ID（仅 ROLE_SCHOOL_LEADER 有值） */
    private final Long schoolId;

    /** 权限列表 */
    private final Collection<? extends GrantedAuthority> authorities;

    public SecurityUser(String username, String roleType, Long orgId, Long districtId, Long schoolId) {
        this.username = username;
        this.roleType = roleType;
        this.orgId = orgId;
        this.districtId = districtId;
        this.schoolId = schoolId;
        // 根据角色类型构建 GrantedAuthority
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(roleType != null ? roleType : "ROLE_USER"));
    }

    @Override
    public String getPassword() {
        // JWT 方式鉴权无需密码
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
