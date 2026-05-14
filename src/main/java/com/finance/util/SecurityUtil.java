package com.finance.util;

import com.finance.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 获取当前用户工具
 * 安全上下文工具类
 * 从 SecurityContextHolder 中获取当前登录用户的信息，
 * SecurityContextHolder作用：经过 JWT 过滤器解析出 userId,放进 SecurityContextHolder整个请求期间随时可以取出来
 * Service 层调用此工具就能知道是谁在操作，无需从 Controller 层层传参
 */
public class SecurityUtil {

    /**
     * 获取当前登录用户 ID
     *
     * @return
     */
    public static Long getCurrentUserId() {
        //从安全上下文拿到登录信息
        //Authentication为身份认证对象,里面存着：当前登录用户 ID,当前用户角色,是否登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        //判断：没登录 / 登录失效 / 是匿名用户
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BusinessException(401, "未登录或Token已过期");
        }
        //把登录信息里的用户ID取出来
        //auth.getPrincipal()存的就是 当前登录用户的 ID,在 JWT 过滤器里放进去的
        return (Long) auth.getPrincipal();
    }

    /**
     * 获取当前用户角色
     *
     * @return
     */
    public static String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // 未登录则抛异常
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException(401, "未登录或Token已过期");
        }
        // 从权限列表中取出角色,从用户权限列表里取出第一个角色并转成字符串，如果没有角色就默认 USER
        return auth.getAuthorities()
                .stream()
                .findFirst()
                .map(Object::toString)
                .orElse("USER");
    }

    /**
     * 判断是不是管理员
     * @return
     */
    public static boolean isAdmin() {
        return "ADMIN".equals(getCurrentUserRole());
    }
}
