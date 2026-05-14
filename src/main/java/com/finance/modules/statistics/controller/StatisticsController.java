package com.finance.modules.statistics.controller;

import com.finance.common.result.Result;
import com.finance.modules.statistics.service.StatisticsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }


    /**
     * 月度收支总览
     * @param month
     * @return
     */
    @GetMapping("/overview")
    public Result<?> overview(@RequestParam(required = false) String month) {
        return Result.success(statisticsService.getMonthlyOverview(month));
    }


    /**
     * 支出分类统计（饼图数据）
     * @param month
     * @param type
     * @return
     */
    @GetMapping("/category-pie")
    public Result<?> categoryPie(@RequestParam(required = false) String month,
                                 @RequestParam(required = false, defaultValue = "0") int type) {
        return Result.success(statisticsService.getCategoryPieData(month, type));
    }


    /**
     * 月度趋势（折线图数据）
     * @param months
     * @return
     */
    @GetMapping("/trend")
    public Result<?> trend(@RequestParam(defaultValue = "12") int months) {
        return Result.success(statisticsService.getMonthlyTrend(months));
    }

    /**
     * 年度总览
     * @param year
     * @return
     */
    @GetMapping("/yearly")
    public Result<?> yearly(@RequestParam(required = false) Integer year) {
        return Result.success(statisticsService.getYearlyOverview(year));
    }
}
