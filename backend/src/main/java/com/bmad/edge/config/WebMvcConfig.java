package com.bmad.edge.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局 MVC 核心接线板
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 限流哨墙已升格为 AOP 切面 (RateLimitAspect)，此处 MVC 不再负责基于 URL 匹配的兜底拦截
    }
}
