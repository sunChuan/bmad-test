package com.bmad.edge.security;

import com.bmad.edge.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 开放平台专属的简单过滤器 (Story 4.1)
 * 用于识别外部开发者的独立 Open-Token，游离在登录的 JWT 体系之外
 */
public class OpenApiAuthFilter implements Filter {

    // 假设这是分发给生态企业的固化 API-KEY （真实环境可入库）
    private static final String VALID_OPEN_TOKEN = "bmad-open-secret-123456";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getRequestURI();
        if (path.startsWith("/open-api/")) {
            String openToken = request.getHeader("Open-Token");
            
            if (openToken == null || !openToken.equals(VALID_OPEN_TOKEN)) {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                Result<Object> result = Result.error(401, "Invalid or Missing Open-Token for BMAD Open Platform.");
                response.getWriter().write(new ObjectMapper().writeValueAsString(result));
                return;
            }
        }
        
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
