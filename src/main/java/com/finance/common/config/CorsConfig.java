package com.finance.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 *  CorsConfig 跨域配置类
 */
@Configuration
public class CorsConfig {

    /**
     * 注册一个跨域配置组件，让 Spring 接管
     * @return
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        //创建一个跨域规则对象
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*"); // 允许所有前端地址
        config.addAllowedHeader("*");//允许所有请求头
        config.addAllowedMethod("*");//允许所有请求方式
        config.setAllowCredentials(true); // 允许携带 Cookie、Token 进行登录验证

        //把跨域配置应用到所有接口
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
