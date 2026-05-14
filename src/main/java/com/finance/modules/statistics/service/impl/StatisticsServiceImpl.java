package com.finance.modules.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finance.modules.bill.entity.BillCategory;
import com.finance.modules.bill.entity.BillRecord;
import com.finance.modules.bill.mapper.BillCategoryMapper;
import com.finance.modules.bill.mapper.BillRecordMapper;
import com.finance.modules.statistics.dto.CategoryPieVO;
import com.finance.modules.statistics.dto.OverviewVO;
import com.finance.modules.statistics.dto.TrendVO;
import com.finance.modules.statistics.service.StatisticsService;
import com.finance.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
//使用Stream 流式计算
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final BillRecordMapper billRecordMapper;// 账单记录表 Mapper
    private final BillCategoryMapper billCategoryMapper;// 账单分类表 Mapper

    public StatisticsServiceImpl(BillRecordMapper billRecordMapper,
                                  BillCategoryMapper billCategoryMapper) {
        this.billRecordMapper = billRecordMapper;
        this.billCategoryMapper = billCategoryMapper;
    }

    /**
     * 月度收支概览
     * @param month
     * @return
     */
    @Override
    public OverviewVO getMonthlyOverview(String month) {
        Long userId = SecurityUtil.getCurrentUserId();
        // 没传月份，默认查当前月
        if (month == null) {
            month = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        // 调用工具方法，获取当月所有账单
        List<BillRecord> records = getMonthRecords(userId, month);

        // 计算总收入：筛选 type=1，求和
        double income = records.stream().filter(r -> r.getType() == 1)
                .mapToDouble(r -> r.getAmount() != null ? r.getAmount() : 0).sum();
        // 计算总支出：筛选 type=0，求和
        double expense = records.stream().filter(r -> r.getType() == 0)
                .mapToDouble(r -> r.getAmount() != null ? r.getAmount() : 0).sum();

        // 封装返回 VO
        OverviewVO vo = new OverviewVO();
        vo.setMonth(month);
        vo.setIncome(Math.round(income * 100.0) / 100.0);// 保留2位小数
        vo.setExpense(Math.round(expense * 100.0) / 100.0);
        vo.setBalance(Math.round((income - expense) * 100.0) / 100.0);// 结余
        return vo;
    }

    /**
     * 分类饼图数据（支出 / 收入占比）
     * @param month
     * @param type
     * @return
     */
    @Override
    public CategoryPieVO getCategoryPieData(String month, int type) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (month == null) {
            month = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        // 获取当月账单
        List<BillRecord> records = getMonthRecords(userId, month);
        //只保留 收入/支出 类型
        records = records.stream().filter(r -> r.getType() == type).collect(Collectors.toList());

        //总金额
        double totalAmount = records.stream()
                .mapToDouble(r -> r.getAmount() != null ? r.getAmount() : 0).sum();

        // 按分类分组统计
        Map<Long, List<BillRecord>> grouped = records.stream()
                .collect(Collectors.groupingBy(BillRecord::getCategoryId));

        List<CategoryPieVO.CategoryPieItem> items = new ArrayList<>();
        //遍历每个分类，统计金额、占比、次数
        for (Map.Entry<Long, List<BillRecord>> entry : grouped.entrySet()) {
            Long catId = entry.getKey();
            List<BillRecord> catRecords = entry.getValue();
            // 分类总金额
            double amount = catRecords.stream()
                    .mapToDouble(r -> r.getAmount() != null ? r.getAmount() : 0).sum();
            // 查询分类名称、图标
            BillCategory category = billCategoryMapper.selectById(catId);

            // 封装每个饼图项
            CategoryPieVO.CategoryPieItem item = new CategoryPieVO.CategoryPieItem();
            item.setCategoryId(catId);
            item.setCategoryName(category != null ? category.getName() : "未知");
            item.setCategoryIcon(category != null ? category.getIcon() : null);
            item.setAmount(Math.round(amount * 100.0) / 100.0);
            // 占比百分比
            item.setPercent(totalAmount > 0
                    ? String.format("%.2f%%", amount / totalAmount * 100) : "0.00%");
            item.setCount(catRecords.size());
            items.add(item);
        }

        // 按金额降序
        items.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));

        //返回
        CategoryPieVO vo = new CategoryPieVO();
        vo.setTotalAmount(Math.round(totalAmount * 100.0) / 100.0);
        vo.setItems(items);
        return vo;
    }

    /**
     * 近 N 月收支趋势（折线图）
     * @param months
     * @return
     */
    @Override
    public List<TrendVO> getMonthlyTrend(int months) {
        Long userId = SecurityUtil.getCurrentUserId();
        // 限制范围 1~24 个月
        if (months < 1) months = 1;
        if (months > 24) months = 24;

        List<TrendVO> result = new ArrayList<>();
        YearMonth current = YearMonth.now();
        // 循环倒推 N 个月
        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            String monthStr = ym.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            // 每个月的账单
            List<BillRecord> records = getMonthRecords(userId, monthStr);

            // 每月收入
            double income = records.stream().filter(r -> r.getType() == 1)
                    .mapToDouble(r -> r.getAmount() != null ? r.getAmount() : 0).sum();
            // 每月支出
            double expense = records.stream().filter(r -> r.getType() == 0)
                    .mapToDouble(r -> r.getAmount() != null ? r.getAmount() : 0).sum();

            // 封装趋势数据
            TrendVO vo = new TrendVO();
            vo.setMonth(monthStr);
            vo.setIncome(Math.round(income * 100.0) / 100.0);
            vo.setExpense(Math.round(expense * 100.0) / 100.0);
            vo.setBalance(Math.round((income - expense) * 100.0) / 100.0);
            result.add(vo);
        }
        return result;
    }

    /**
     * 年度财务报告（全年总结）
     * @param year
     * @return
     */
    @Override
    public Object getYearlyOverview(Integer year) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        // 初始化统计变量
        double totalIncome = 0;
        double totalExpense = 0;
        String highestExpenseMonth = null;
        double highestExpenseAmount = 0;
        String lowestExpenseMonth = null;
        double lowestExpenseAmount = Double.MAX_VALUE;
        int monthCount = 0;

        // 循环 12 个月
        for (int m = 1; m <= 12; m++) {
            String monthStr = String.format("%d-%02d", year, m);
            List<BillRecord> records = getMonthRecords(userId, monthStr);
            if (records.isEmpty()) continue;

            monthCount++;
            double income = records.stream().filter(r -> r.getType() == 1)
                    .mapToDouble(r -> r.getAmount() != null ? r.getAmount() : 0).sum();
            double expense = records.stream().filter(r -> r.getType() == 0)
                    .mapToDouble(r -> r.getAmount() != null ? r.getAmount() : 0).sum();

            // 累计全年收支
            totalIncome += income;
            totalExpense += expense;

            // 找出最高消费月
            if (expense > highestExpenseAmount) {
                highestExpenseAmount = expense;
                highestExpenseMonth = monthStr;
            }
            // 找出最低消费月
            if (expense < lowestExpenseAmount) {
                lowestExpenseAmount = expense;
                lowestExpenseMonth = monthStr;
            }
        }

        if (lowestExpenseAmount == Double.MAX_VALUE) lowestExpenseAmount = 0;

        // 封装年度报告
        Map<String, Object> result = new HashMap<>();
        result.put("year", year);
        result.put("totalIncome", Math.round(totalIncome * 100.0) / 100.0);
        result.put("totalExpense", Math.round(totalExpense * 100.0) / 100.0);
        result.put("balance", Math.round((totalIncome - totalExpense) * 100.0) / 100.0);
        result.put("monthlyAvgExpense", monthCount > 0
                ? Math.round(totalExpense / monthCount * 100.0) / 100.0 : 0);
        result.put("highestExpenseMonth", highestExpenseMonth);
        result.put("highestExpenseAmount", Math.round(highestExpenseAmount * 100.0) / 100.0);
        result.put("lowestExpenseMonth", lowestExpenseMonth);
        result.put("lowestExpenseAmount", Math.round(lowestExpenseAmount * 100.0) / 100.0);
        return result;
    }

    /**
     * 按月份查询当前用户账单
     * @param userId
     * @param month
     * @return
     */
    private List<BillRecord> getMonthRecords(Long userId, String month) {
        // 把字符串 "2025-12" 转成年月对象
        YearMonth ym = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
        // 拼接 月初 00:00:00
        String start = ym.atDay(1).atStartOfDay()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // 拼接 月末 23:59:59
        String end = ym.atEndOfMonth().atTime(23, 59, 59)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 查询：当前用户 + 时间范围
        return billRecordMapper.selectList(
                new LambdaQueryWrapper<BillRecord>()
                        .eq(BillRecord::getUserId, userId)
                        .ge(BillRecord::getRecordTime, start)
                        .le(BillRecord::getRecordTime, end));
    }
}
