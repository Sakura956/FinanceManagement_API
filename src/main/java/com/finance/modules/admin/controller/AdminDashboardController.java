package com.finance.modules.admin.controller;

import com.finance.common.result.Result;
import com.finance.modules.admin.service.AdminDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    // 管理端仪表盘
    @GetMapping("/dashboard")
    public Result<?> dashboard() {
        return Result.success(adminDashboardService.getDashboard());
    }
}
