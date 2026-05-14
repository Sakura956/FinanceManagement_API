package com.finance.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finance.modules.auth.entity.User;
import com.finance.modules.auth.mapper.UserMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 用户信息加载
 * 实现 UserDetailsService 接口，
 * Spring Security 在认证时会调用 loadUserByUsername 方法，
 * 根据手机号从数据库查询用户信息，返回 UserDetails 对象
 * 登录时根据手机号 → 去数据库查用户 → 封装成 Security 认识的用户对象
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;//用来查数据库用户表

    public UserDetailsServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 登录时，Spring Security 自动调用这个方法
     * @param phone
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone));

        if (user == null) {
            throw new UsernameNotFoundException("手机号或密码错误");
        }

        //封装成 Spring Security 认识的用户
        return new org.springframework.security.core.userdetails.User(
                String.valueOf(user.getId()),// 用户名（我们放用户ID）
                user.getPassword(),// 数据库加密密码
                user.getStatus() != null && user.getStatus() == 1 ? false : true,
                true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
    }
}
