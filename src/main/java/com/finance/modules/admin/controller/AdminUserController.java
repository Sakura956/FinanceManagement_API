package com.finance.modules.admin.controller;

import com.finance.common.result.Result;
import com.finance.modules.admin.dto.UserStatusRequest;
import com.finance.modules.admin.service.AdminUserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;
    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    //用户列表查询
    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Integer status,
                          @RequestParam(required = false) String startDate,
                          @RequestParam(required = false) String endDate) {
        return Result.success(adminUserService.listUsers(page, size, keyword, status, startDate, endDate));
    }

    //用户详情，管理员查看指定用户的详细信息及统计数据。
    @GetMapping("/{userId}")
    public Result<?> detail(@PathVariable Long userId) {
        return Result.success(adminUserService.getUserDetail(userId));
    }

    //封禁/解封用户
    @PutMapping("/{userId}/status")
    public Result<Void> updateStatus(@PathVariable Long userId, @RequestBody UserStatusRequest request) {
        adminUserService.updateUserStatus(userId, request);
        return Result.success("用户状态更新成功", null);
    }
}
