package com.finance.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finance.modules.admin.dto.DashboardVO;
import com.finance.modules.admin.service.AdminDashboardService;
import com.finance.modules.auth.entity.User;
import com.finance.modules.auth.mapper.UserMapper;
import com.finance.modules.bill.entity.BillRecord;
import com.finance.modules.bill.mapper.BillRecordMapper;
import com.finance.modules.memo.entity.Memo;
import com.finance.modules.memo.mapper.MemoMapper;
import com.finance.modules.plan.entity.FinancePlan;
import com.finance.modules.plan.mapper.FinancePlanMapper;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserMapper userMapper;// 用户表
    private final BillRecordMapper billRecordMapper;// 账单表
    private final FinancePlanMapper financePlanMapper;// 理财计划表
    private final MemoMapper memoMapper;// 备忘录表

    public AdminDashboardServiceImpl(UserMapper userMapper, BillRecordMapper billRecordMapper,
                                     FinancePlanMapper financePlanMapper, MemoMapper memoMapper) {
        this.userMapper = userMapper;
        this.billRecordMapper = billRecordMapper;
        this.financePlanMapper = financePlanMapper;
        this.memoMapper = memoMapper;
    }

    /**
     * 获取后台首页所有统计数据
     * @return
     */
    @Override
    public DashboardVO getDashboard() {
        // 今天 00:00:00
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        // 明天 00:00:00（今天结束）
        LocalDateTime todayEnd = todayStart.plusDays(1);
        // 本周一 00:00:00
        LocalDateTime weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        // 本月1号 00:00:00
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        DashboardVO vo = new DashboardVO();
        //总用户数
        vo.setTotalUsers(userMapper.selectCount(null));
        //今日活跃用户,今天更新过资料的
        vo.setActiveUsersToday(userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getUpdateTime, todayStart)));
        //本周新增用户
        vo.setNewUsersThisWeek(userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreateTime, weekStart)));
        //本月新增用户
        vo.setNewUsersThisMonth(userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreateTime, monthStart)));
        //总账单数
        vo.setTotalBills(billRecordMapper.selectCount(null));
        //今日新增账单
        vo.setBillsToday(billRecordMapper.selectCount(
                new LambdaQueryWrapper<BillRecord>().ge(BillRecord::getCreateTime, todayStart)
                        .lt(BillRecord::getCreateTime, todayEnd)));
        //总理财计划数
        vo.setTotalPlans(financePlanMapper.selectCount(null));
        //总备忘录数
        vo.setTotalMemos(memoMapper.selectCount(null));
        return vo;
    }
}
