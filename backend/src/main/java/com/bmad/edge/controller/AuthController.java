package com.bmad.edge.controller;

import com.bmad.edge.common.Result;
import com.bmad.edge.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器。
 * 处理 SSO 回调，根据模拟的角色类型生成含 RBAC 属性的 JWT Token。
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * SSO 回调接口。
     * 真实场景下：通过 code 换取 SSO AccessToken -> 拉取用户信息 -> 提取角色/组织属性 -> 签发 JWT。
     * 当前为 Mock 实现，根据 code 前缀模拟不同角色。
     *
     * @param code SSO 授权码
     */
    @GetMapping("/sso-callback")
    public Result<Map<String, Object>> ssoCallback(@RequestParam String code) {
        // 根据 code 前缀模拟不同角色的用户
        String username;
        String roleType;
        Long orgId;
        Long districtId = null;
        Long schoolId = null;

        if (code.startsWith("city_")) {
            // 模拟市级超级管理员
            username = "city_admin_" + code;
            roleType = "ROLE_CITY_ADMIN";
            orgId = 100L; // 市级组织 ID
        } else if (code.startsWith("district_")) {
            // 模拟区县局长
            username = "district_director_" + code;
            roleType = "ROLE_DISTRICT_DIRECTOR";
            orgId = 200L;
            // 尝试从 code 获取 districtId，例如 district_301 -> 301L，否则默认 201
            try {
                String idStr = code.substring(code.lastIndexOf("_") + 1);
                districtId = Long.parseLong(idStr);
            } catch (Exception e) {
                districtId = 201L;
            }
        } else if (code.startsWith("school_")) {
            // 模拟校级领导
            username = "school_leader_" + code;
            roleType = "ROLE_SCHOOL_LEADER";
            orgId = 300L;
            districtId = 301L; // 所属区县 ID
            schoolId = 3001L; // 学校 ID
        } else {
            // 默认角色
            username = "user_" + code;
            roleType = "ROLE_CITY_ADMIN";
            orgId = 1L;
        }

        String jwt = jwtUtils.generateToken(username, roleType, orgId, districtId, schoolId);

        Map<String, Object> data = new HashMap<>();
        data.put("token", jwt);
        data.put("roleType", roleType);
        data.put("username", username);

        return Result.success(data);
    }
}
