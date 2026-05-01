package com.ai.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 允许任意路径
                .allowedOriginPatterns("*") // 允许匹配所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许特定请求方法
                .allowCredentials(true) // 允许携带凭证
                .maxAge(3600); // 预检请求的缓存时间
    }
}