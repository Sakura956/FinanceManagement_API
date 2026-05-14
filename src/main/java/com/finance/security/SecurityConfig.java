package com.finance.security;

import com.finance.util.RedisUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置
 * 哪些接口不需要认证
 * 哪些接口需要管理员角色（
 * 注册 JwtAuthenticationFilter 到过滤器链
 * 配置密码加密器
 */
@Configuration
@EnableWebSecurity// 开启 Spring Security 安全功能
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;//JWT 工具
    private final RedisUtil redisUtil;//Redis 工具
    private final CustomAccessDeniedHandler accessDeniedHandler;//权限不足处理器

    public SecurityConfig(JwtTokenProvider jwtTokenProvider, RedisUtil redisUtil,
                          CustomAccessDeniedHandler accessDeniedHandler) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisUtil = redisUtil;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    /**
     * 核心配置方法:配置安全规则
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())// 这只是关闭csrf，不是跨域
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))//设置无状态（不使用 session）
            .authorizeHttpRequests(auth -> auth
                // 认证接口 - 无需认证
                .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login").permitAll()
                // 管理端接口 - 需要 ADMIN 角色
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                // 用户端接口 - 需要认证
                .requestMatchers("/api/v1/user/**").authenticated()
                // 其他请求允许
                .anyRequest().permitAll()
            )
            .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler))//权限不足时使用自定义处理器
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, redisUtil),
                    UsernamePasswordAuthenticationFilter.class);//加入 JWT 过滤器

        //构建并返回配置
        return http.build();
    }

    /**
     * 密码加密器
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     * Spring Security 进行登录验证
     * @param config
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
