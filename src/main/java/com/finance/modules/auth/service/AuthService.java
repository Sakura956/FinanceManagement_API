package com.finance.modules.auth.service;

import com.finance.modules.auth.dto.ChangePasswordRequest;
import com.finance.modules.auth.dto.LoginRequest;
import com.finance.modules.auth.dto.LoginResponse;
import com.finance.modules.auth.dto.RegisterRequest;
import com.finance.modules.auth.entity.User;

import java.util.Map;

public interface AuthService {

    //注册
    void register(RegisterRequest request);

    //登录
    LoginResponse login(LoginRequest request);

    //修改密码
    void changePassword(ChangePasswordRequest request);

    ////获取当前登录用户信息
    User me();

    //更新个人信息
    User updateProfile(Map<String, Object> params);
}
