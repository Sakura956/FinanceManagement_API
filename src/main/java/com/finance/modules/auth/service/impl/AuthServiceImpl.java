package com.finance.modules.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finance.common.exception.BusinessException;
import com.finance.modules.auth.dto.ChangePasswordRequest;
import com.finance.modules.auth.dto.LoginRequest;
import com.finance.modules.auth.dto.LoginResponse;
import com.finance.modules.auth.dto.RegisterRequest;
import com.finance.modules.auth.entity.User;
import com.finance.modules.auth.mapper.UserMapper;
import com.finance.modules.auth.service.AuthService;
import com.finance.security.JwtTokenProvider;
import com.finance.util.RedisUtil;
import com.finance.util.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;// 操作用户表
    private final PasswordEncoder passwordEncoder;// 密码加密、比对
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisUtil redisUtil;

    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder,JwtTokenProvider jwtTokenProvider,RedisUtil redisUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisUtil = redisUtil;
    }

    /**
     * 注册
     * @param request
     */
    @Override
    public void register(RegisterRequest request) {
        // 检查手机号是否已注册
        if (userMapper.selectCount(
                //LambdaQueryWrapper：MyBatis-Plus 条件构造器
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone())) > 0) {
            throw new BusinessException(409, "该手机号已注册");
        }

        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getPhone());
        user.setRole("USER");
        user.setStatus(0);
        userMapper.insert(user);
    }

    /**
     * 登录
     * @param request
     * @return
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        //LambdaQueryWrapper<User>()：MP 的条件构造器
        //.eq(User::getPhone, request.getPhone()) -> 等于：where phone = 前端传的手机号
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));

        //!passwordEncoder.matches(明文密码, 加密密码)
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "手机号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new BusinessException(403, "账号已被封禁，请联系管理员");
        }

        //把 userId + role 放进 Token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole());
        // 存入Redis，TTL 7天
        redisUtil.set("token:user:" + user.getId(), token, 7 * 24 * 3600, java.util.concurrent.TimeUnit.SECONDS);

        //构建要返回给前端的用户信息
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .role(user.getRole())
                .avatarUrl(user.getAvatarUrl())
                .build();

        return LoginResponse.builder()
                .token(token)
                .userInfo(userInfo)
                .build();
    }

    /**
     * 修改密码
     * @param request
     */
    @Override
    public void changePassword(ChangePasswordRequest request) {
        //获取当前登录用户 ID
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "旧密码错误");
        }
        //更新新密码（加密）
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
        // 清除Redis中的Token，强制重新登录
        redisUtil.delete("token:user:" + userId);
    }

    /**
     * 获取当前登录用户信息
     * @return
     */
    @Override
    public User me() {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setPassword(null); // 不暴露密码
        return user;
    }

    /**
     * 更新个人信息
     * @param params
     * @return
     */
    @Override
    public User updateProfile(Map<String, Object> params) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (params.containsKey("nickname")) {
            user.setNickname((String) params.get("nickname"));
        }
        if (params.containsKey("avatarUrl")) {
            user.setAvatarUrl((String) params.get("avatarUrl"));
        }
        userMapper.updateById(user);
        user.setPassword(null);
        return user;
    }
}
