package com.finance.modules.plan.controller;

import com.finance.common.result.Result;
import com.finance.modules.plan.dto.PlanCreateRequest;
import com.finance.modules.plan.dto.PlanUpdateRequest;
import com.finance.modules.plan.dto.PlanValuationRequest;
import com.finance.modules.plan.service.FinancePlanService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user/finance-plans")
public class FinancePlanController {

    private final FinancePlanService financePlanService;

    public FinancePlanController(FinancePlanService financePlanService) {
        this.financePlanService = financePlanService;
    }


    /**
     * 获取理财计划列表
     *
     * @param status
     * @param page
     * @param size
     * @return
     */
    @GetMapping
    public Result<?> list(@RequestParam(required = false) Integer status,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size) {
        return Result.success(financePlanService.listPlans(status, page, size));
    }


    /**
     * 理财计划详情
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.success(financePlanService.getPlanDetail(id));
    }


    /**
     * 创建理财计划
     *
     * @param request
     * @return
     */
    @PostMapping
    public Result<?> create(@Valid @RequestBody PlanCreateRequest request) {
        return Result.success("理财计划创建成功", financePlanService.createPlan(request));
    }


    /**
     * 修改理财计划基本信息
     *
     * @param id
     * @param request
     * @return
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody PlanUpdateRequest request) {
        return Result.success("理财计划更新成功", financePlanService.updatePlan(id, request));
    }


    /**
     * 更新理财计划市值/收益
     *
     * @param id
     * @param request
     * @return
     */
    @PutMapping("/{id}/valuation")
    public Result<?> updateValuation(@PathVariable Long id, @RequestBody PlanValuationRequest request) {
        return Result.success("市值更新成功", financePlanService.updateValuation(id, request));
    }


    /**
     * 理财计划状态变更（赎回）
     *
     * @param id
     * @param body
     * @return
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        financePlanService.updatePlanStatus(id, (int) body.get("status"), (String) body.get("endDate"));
        return Result.success("该理财计划已标记为已赎回", null);
    }


    /**
     * 删除理财计划
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        financePlanService.deletePlan(id);
        return Result.success("理财计划删除成功", null);
    }
}
