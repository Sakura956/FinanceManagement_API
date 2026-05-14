package com.finance.modules.auth.controller;

import com.finance.common.result.Result;
import com.finance.modules.auth.dto.ChangePasswordRequest;
import com.finance.modules.auth.dto.LoginRequest;
import com.finance.modules.auth.dto.RegisterRequest;
import com.finance.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    //final 关键字保证线程安全、不可变，优于@Autowired
    private final AuthService authService;
    //构造器注入
    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    /**
     * 注册
     * @param request
     * @return
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success("注册成功", null);
    }

    /**
     * 登录
     * @param request
     * @return
     */
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("登录成功", authService.login(request));
    }


    /**
     * 修改密码
     * @param request
     * @return
     */
    @PutMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return Result.success("密码修改成功，请重新登录", null);
    }


    /**
     * 获取当前登录用户信息
     * @return
     */
    @GetMapping("/me")
    public Result<?> me() {
        return Result.success(authService.me());
    }


    /**
     * 更新个人信息
     * @param params
     * @return
     */
    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody Map<String, Object> params) {
        return Result.success("个人信息更新成功", authService.updateProfile(params));
    }
}
