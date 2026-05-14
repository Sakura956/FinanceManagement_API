package com.finance.modules.statistics.service;

import com.finance.modules.statistics.dto.OverviewVO;
import com.finance.modules.statistics.dto.CategoryPieVO;
import com.finance.modules.statistics.dto.TrendVO;

import java.util.List;

public interface StatisticsService {

    OverviewVO getMonthlyOverview(String month);

    CategoryPieVO getCategoryPieData(String month, int type);

    List<TrendVO> getMonthlyTrend(int months);

    Object getYearlyOverview(Integer year);
}
