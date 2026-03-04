package com.bmad.edge.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开放入口限流注解 (Story 4.2)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /**
     * 限流的 Key 维度，默认针对于访问 IP，也可以配置 Token 或者 AppId 进行区分
     */
    String key() default "open_api_rate_limit:";

    /**
     * 周期内允许多少次访问请求 (QPS上限控制)
     */
    int limit() default 100;

    /**
     * 滑动窗口的时长尺寸，默认 1 秒
     */
    int timeout() default 1;
}
