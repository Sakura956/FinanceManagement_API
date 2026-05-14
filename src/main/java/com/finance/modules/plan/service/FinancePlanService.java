package com.finance.modules.plan.service;

import com.finance.common.result.PageResult;
import com.finance.modules.plan.dto.PlanCreateRequest;
import com.finance.modules.plan.dto.PlanUpdateRequest;
import com.finance.modules.plan.dto.PlanValuationRequest;

public interface FinancePlanService {

    Object listPlans(Integer status, int page, int size);

    Object getPlanDetail(Long id);

    Object createPlan(PlanCreateRequest request);

    Object updatePlan(Long id, PlanUpdateRequest request);

    Object updateValuation(Long id, PlanValuationRequest request);

    void updatePlanStatus(Long id, int status, String endDate);

    void deletePlan(Long id);
}
