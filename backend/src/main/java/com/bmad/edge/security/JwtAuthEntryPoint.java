package com.bmad.edge.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 认证失败入口处理器。
 * 当未认证或 Token 无效时返回 401 状态码与统一 JSON 响应格式，
 * 同时记录审计日志以满足安全合规要求。
 */
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");
    private static final Logger log = LoggerFactory.getLogger(JwtAuthEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // 记录审计日志：认证失败事件
        String requestUri = request.getRequestURI();
        String remoteAddr = request.getRemoteAddr();
        String authHeader = request.getHeader("Authorization");
        boolean hasToken = authHeader != null && authHeader.startsWith("Bearer ");

        auditLog.warn("[AUTH_FAILURE] URI={}, IP={}, hasToken={}, reason={}",
                requestUri, remoteAddr, hasToken,
                authException != null ? authException.getMessage() : "unknown");
        log.warn("认证失败 - URI: {}, IP: {}, 原因: {}", requestUri, remoteAddr,
                authException != null ? authException.getMessage() : "unknown");

        // 返回统一 JSON 格式的 401 响应
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> body = new HashMap<>();
        body.put("code", 401);
        body.put("message", "认证失败：" + (authException != null ? authException.getMessage() : "无效的访问凭证"));
        body.put("data", null);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
