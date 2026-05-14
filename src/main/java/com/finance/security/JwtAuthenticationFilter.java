package com.finance.security;

import com.finance.util.RedisUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT过滤器
 */
//这是一个 过滤器,OncePerRequestFilter 代表 每个请求只执行一次
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;// JWT 工具
    private final RedisUtil redisUtil;// Redis 工具

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, RedisUtil redisUtil) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisUtil = redisUtil;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,// 请求（前端发来的）
                                    HttpServletResponse response,// 响应
                                    FilterChain filterChain// 过滤器链（走完继续走后面）
    ) throws ServletException, IOException {
        //从请求头里取出 Token
        String token = resolveToken(request);

        //判断 Token 是否有效
        //StringUtils.hasText(token)判断字符串 是否 不为 null、不为空、不只含空格
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            //解析 Token 拿到 userId 和 role
            Claims claims = jwtTokenProvider.parseToken(token);
            Long userId = claims.get("userId", Long.class);
            String role = claims.get("role", String.class);

            //去 Redis 检查是否真的登录
            String redisToken = redisUtil.get("token:user:" + userId);

            if (redisToken != null && redisToken.equals(token)) {
                //把登录信息放入 Spring Security 上下文
                //UsernamePasswordAuthenticationToken 是Spring Security 官方提供的登录认证后的 “用户身份凭证”
                //只要把它放进 SecurityContext，Spring 就认为该用户已登录
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
                //SimpleGrantedAuthority是Spring Security 规定的 权限 / 角色 包装类,格式必须是ROLE_ADMIN
                // Collections.singletonList(xxx)是快速创建 只有 1 个元素 的 不可变 List


                //使在SecurityUtil类里可以拿到解析后的token信息
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        //放行请求
        filterChain.doFilter(request, response);
    }


    /**
     * 从请求头取出 Token
     *
     * @param request
     * @return
     */
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");

        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);//去掉前面Bearer 7 个字符
        }
        return null;
    }
}
