package com.finance.modules.admin.dto;

import lombok.Data;

/**
 * 仪表盘响应 VO
 * 封装仪表盘返回的统计数据
 */
@Data
public class DashboardVO {

    private Long totalUsers;

    private Long activeUsersToday;

    private Long newUsersThisWeek;

    private Long newUsersThisMonth;

    private Long totalBills;

    private Long billsToday;

    private Long totalPlans;

    private Long totalMemos;
}
