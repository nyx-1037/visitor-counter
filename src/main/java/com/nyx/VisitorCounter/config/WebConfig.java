package com.nyx.visitorcounter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 提供全局CORS配置，解决跨域问题
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置全局CORS
     * 允许所有来源的跨域请求访问API
     * 特别针对访问量统计接口进行优化配置
     * 
     * @param registry CORS注册表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 全局CORS配置
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // 使用allowedOriginPatterns代替allowedOrigins以支持更灵活的配置
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials", "Content-Type", "Content-Length")
                .allowCredentials(false) // 当allowedOriginPatterns为*时，不能设置为true
                .maxAge(3600); // 预检请求的有效期，单位为秒
                
        // 专门为访问量统计接口配置更宽松的CORS策略
        registry.addMapping("/api/visitor/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(false)
                .maxAge(86400); // 24小时缓存
    }
}