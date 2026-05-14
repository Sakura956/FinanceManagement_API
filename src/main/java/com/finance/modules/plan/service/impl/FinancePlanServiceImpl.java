package com.finance.modules.plan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.finance.common.exception.BusinessException;
import com.finance.common.result.PageResult;
import com.finance.modules.plan.dto.PlanCreateRequest;
import com.finance.modules.plan.dto.PlanUpdateRequest;
import com.finance.modules.plan.dto.PlanVO;
import com.finance.modules.plan.dto.PlanValuationRequest;
import com.finance.modules.plan.entity.FinancePlan;
import com.finance.modules.plan.mapper.FinancePlanMapper;
import com.finance.modules.plan.service.FinancePlanService;
import com.finance.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinancePlanServiceImpl implements FinancePlanService {

    private final FinancePlanMapper financePlanMapper;

    public FinancePlanServiceImpl(FinancePlanMapper financePlanMapper) {
        this.financePlanMapper = financePlanMapper;
    }

    /**
     * 实体 → VO（自动算收益）
     * @param plan
     * @return
     */
    private PlanVO toPlanVO(FinancePlan plan) {
        PlanVO vo = new PlanVO();
        vo.setId(plan.getId());
        vo.setName(plan.getName());
        vo.setInitialAmount(plan.getInitialAmount());// 初始本金
        vo.setCurrentValue(plan.getCurrentValue());// 当前市值
        if (plan.getInitialAmount() != null && plan.getCurrentValue() != null) {
            // 收益 = 当前价值 - 初始本金
            double profit = plan.getCurrentValue() - plan.getInitialAmount();
            vo.setProfitAmount(Math.round(profit * 100.0) / 100.0);// 保留2位小数
            // 收益率 = 收益 / 本金
            if (plan.getInitialAmount() > 0) {
                vo.setProfitRate(Math.round(profit / plan.getInitialAmount() * 10000.0) / 100.0);
            }
        }
        vo.setExpectedRoi(plan.getExpectedRoi());// 预期收益率
        vo.setStartDate(plan.getStartDate() != null ? plan.getStartDate().toString() : null);
        vo.setEndDate(plan.getEndDate() != null ? plan.getEndDate().toString() : null);
        vo.setStatus(plan.getStatus());
        vo.setRemark(plan.getRemark());
        vo.setCreateTime(plan.getCreateTime() != null ? plan.getCreateTime().toString() : null);
        vo.setUpdateTime(plan.getUpdateTime() != null ? plan.getUpdateTime().toString() : null);
        return vo;
    }

    /**
     * 分页查询理财计划 + 统计汇总
     * @param status
     * @param page
     * @param size
     * @return
     */
    @Override
    public Object listPlans(Integer status, int page, int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        LambdaQueryWrapper<FinancePlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinancePlan::getUserId, userId);
        if (status != null) {
            wrapper.eq(FinancePlan::getStatus, status);
        }
        wrapper.orderByDesc(FinancePlan::getCreateTime);

        Page<FinancePlan> result = financePlanMapper.selectPage(new Page<>(page, size), wrapper);

        //转为 VO 列表
        List<PlanVO> records = new ArrayList<>();
        for (FinancePlan plan : result.getRecords()) {
            records.add(toPlanVO(plan));
        }

        // 汇总统计（所有计划，不受分页影响）
        LambdaQueryWrapper<FinancePlan> summaryWrapper = new LambdaQueryWrapper<>();
        summaryWrapper.eq(FinancePlan::getUserId, userId);
        if (status != null) {
            summaryWrapper.eq(FinancePlan::getStatus, status);
        }
        List<FinancePlan> allPlans = financePlanMapper.selectList(summaryWrapper);
        //统计总投入
        double totalInvested = allPlans.stream().mapToDouble(p -> p.getInitialAmount() != null ? p.getInitialAmount() : 0).sum();
        //总市值
        double totalCurrentValue = allPlans.stream().mapToDouble(p -> p.getCurrentValue() != null ? p.getCurrentValue() : 0).sum();
        //总收益
        double totalProfit = totalCurrentValue - totalInvested;
        //总收益率
        double overallProfitRate = totalInvested > 0 ? Math.round(totalProfit / totalInvested * 10000.0) / 100.0 : 0;

        //封装返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("total", result.getTotal());
        response.put("page", page);
        response.put("size", size);
        response.put("pages", (int) Math.ceil((double) result.getTotal() / size));
        response.put("records", records);
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalInvested", Math.round(totalInvested * 100.0) / 100.0);
        summary.put("totalCurrentValue", Math.round(totalCurrentValue * 100.0) / 100.0);
        summary.put("totalProfit", Math.round(totalProfit * 100.0) / 100.0);
        summary.put("overallProfitRate", overallProfitRate);
        response.put("summary", summary);
        return response;
    }

    /**
     * 获取理财计划详情
     * @param id
     * @return
     */
    @Override
    public Object getPlanDetail(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        FinancePlan plan = financePlanMapper.selectOne(
                new LambdaQueryWrapper<FinancePlan>()
                        .eq(FinancePlan::getId, id)
                        .eq(FinancePlan::getUserId, userId));
        if (plan == null) {
            throw new BusinessException(404, "理财计划不存在");
        }
        return toPlanVO(plan);
    }

    /**
     * 创建理财计划
     * @param request
     * @return
     */
    @Override
    public Object createPlan(PlanCreateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        FinancePlan plan = new FinancePlan();
        plan.setUserId(userId);
        plan.setName(request.getName());
        plan.setInitialAmount(request.getInitialAmount());
        plan.setCurrentValue(request.getCurrentValue());
        plan.setExpectedRoi(request.getExpectedRoi());
        // 日期转换 String → LocalDate
        plan.setStartDate(LocalDate.parse(request.getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        if (StringUtils.hasText(request.getEndDate())) {
            plan.setEndDate(LocalDate.parse(request.getEndDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        plan.setRemark(request.getRemark());
        plan.setStatus(0);
        financePlanMapper.insert(plan);
        return toPlanVO(plan);
    }

    /**
     * 修改理财计划
     * @param id
     * @param request
     * @return
     */
    @Override
    public Object updatePlan(Long id, PlanUpdateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        FinancePlan plan = financePlanMapper.selectOne(
                new LambdaQueryWrapper<FinancePlan>()
                        .eq(FinancePlan::getId, id)
                        .eq(FinancePlan::getUserId, userId));
        if (plan == null) {
            throw new BusinessException(404, "理财计划不存在");
        }
        if (StringUtils.hasText(request.getName())) {
            plan.setName(request.getName());
        }
        if (request.getInitialAmount() != null) {
            plan.setInitialAmount(request.getInitialAmount());
        }
        if (request.getCurrentValue() != null) {
            plan.setCurrentValue(request.getCurrentValue());
        }
        if (request.getExpectedRoi() != null) {
            plan.setExpectedRoi(request.getExpectedRoi());
        }
        if (request.getRemark() != null) {
            plan.setRemark(request.getRemark());
        }
        if (StringUtils.hasText(request.getStartDate())) {
            plan.setStartDate(LocalDate.parse(request.getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (request.getEndDate() != null) {
            plan.setEndDate(StringUtils.hasText(request.getEndDate())
                    ? LocalDate.parse(request.getEndDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null);
        }
        financePlanMapper.updateById(plan);
        return toPlanVO(plan);
    }

    /**
     * 更新估值（市值）
     * @param id
     * @param request
     * @return
     */
    @Override
    public Object updateValuation(Long id, PlanValuationRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        FinancePlan plan = financePlanMapper.selectOne(
                new LambdaQueryWrapper<FinancePlan>()
                        .eq(FinancePlan::getId, id)
                        .eq(FinancePlan::getUserId, userId));
        if (plan == null) {
            throw new BusinessException(404, "理财计划不存在");
        }
        // 更新当前市值
        plan.setCurrentValue(request.getCurrentValue());
        financePlanMapper.updateById(plan);

        //返回最新收益
        Map<String, Object> result = new HashMap<>();
        result.put("id", plan.getId());
        result.put("initialAmount", plan.getInitialAmount());
        result.put("currentValue", plan.getCurrentValue());
        double profit = plan.getCurrentValue() - plan.getInitialAmount();
        result.put("profitAmount", Math.round(profit * 100.0) / 100.0);
        result.put("profitRate", plan.getInitialAmount() > 0
                ? Math.round(profit / plan.getInitialAmount() * 10000.0) / 100.0 : 0);
        result.put("updateTime", plan.getUpdateTime() != null ? plan.getUpdateTime().toString() : null);
        return result;
    }

    /**
     * 更新计划状态
     * @param id
     * @param status
     * @param endDate
     */
    @Override
    public void updatePlanStatus(Long id, int status, String endDate) {
        Long userId = SecurityUtil.getCurrentUserId();
        FinancePlan plan = financePlanMapper.selectOne(
                new LambdaQueryWrapper<FinancePlan>()
                        .eq(FinancePlan::getId, id)
                        .eq(FinancePlan::getUserId, userId));
        if (plan == null) {
            throw new BusinessException(404, "理财计划不存在");
        }
        plan.setStatus(status);
        // 如果有传入结束时间则用传入的
        if (StringUtils.hasText(endDate)) {
            plan.setEndDate(LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else if (status == 1) {
            plan.setEndDate(LocalDate.now());
        }
        financePlanMapper.updateById(plan);
    }

    /**
     * 删除理财计划
     * @param id
     */
    @Override
    public void deletePlan(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        FinancePlan plan = financePlanMapper.selectOne(
                new LambdaQueryWrapper<FinancePlan>()
                        .eq(FinancePlan::getId, id)
                        .eq(FinancePlan::getUserId, userId));
        if (plan == null) {
            throw new BusinessException(404, "理财计划不存在");
        }
        financePlanMapper.deleteById(id);
    }
}
