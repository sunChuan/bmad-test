package com.bmad.edge.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * MyBatis-Plus 配置类。
 * 注册 TenantLineInnerInterceptor 实现基于 district_id 的透明行级数据隔离。
 * 
 * 设计决策：
 * - 采用「共享数据库 / 共享 Schema」模式，通过 SQL 层自动注入租户条件
 * - 对业务代码完全透明，开发者无需在 Service/Mapper 中手写 district_id 过滤
 * - ROLE_CITY_ADMIN 和 ROLE_SANDBOX 角色跳过租户过滤（全市数据可见）
 */
@Configuration
public class MybatisPlusConfig {

    /** 不参与租户隔离的系统级表 */
    private static final List<String> IGNORE_TABLES = Arrays.asList(
            "sys_config",       // 系统配置表
            "sys_dict",         // 数据字典表
            "sys_audit_log",    // 审计日志表（跨租户可查）
            "sys_user",         // 用户表（登录时需跨租户查询）
            "sys_role",         // 角色表
            "sys_menu"          // 菜单表
    );

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 租户拦截器 - 必须在第一个位置注册，保证最先执行
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {

            /**
             * 获取当前租户 ID（district_id）。
             * 从 SecurityContext -> SecurityUser 中提取 districtId。
             */
            @Override
            public Expression getTenantId() {
                Long tenantId = TenantContextHolder.getCurrentTenantId();
                if (tenantId == null) {
                    // 无租户上下文时返回 NullValue，配合 ignoreTable 逻辑使用
                    return new NullValue();
                }
                return new LongValue(tenantId);
            }

            /**
             * 租户字段名：所有业务表统一使用 district_id 作为租户隔离列
             */
            @Override
            public String getTenantIdColumn() {
                return "district_id";
            }

            /**
             * 判断是否跳过租户过滤。
             * 以下情况跳过：
             * 1. 表在忽略列表中（系统级表）
             * 2. 当前用户是市级管理员（ROLE_CITY_ADMIN）- 全市数据可见
             * 3. 当前用户是沙箱特权账号（ROLE_SANDBOX）- 试算需要全量数据
             * 4. 无认证上下文（公开接口，但已被 Security 层拦截）
             */
            @Override
            public boolean ignoreTable(String tableName) {
                // 系统级表不参与租户隔离
                if (IGNORE_TABLES.contains(tableName)) {
                    return true;
                }

                // 检查当前角色是否有全局数据权限
                String currentRole = TenantContextHolder.getCurrentRole();
                if (currentRole == null) {
                    // 无认证上下文 - 跳过拦截（Security 层已阻断未认证请求）
                    return true;
                }

                // 市级管理员和沙箱特权账号可查看全量数据
                return "ROLE_CITY_ADMIN".equals(currentRole) || "ROLE_SANDBOX".equals(currentRole);
            }
        }));

        return interceptor;
    }
}
