package com.finance.modules.plan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlanValuationRequest {

    @NotNull(message = "当前市值不能为空")
    @DecimalMin(value = "0.00", message = "当前市值必须大于等于0")
    private Double currentValue;
}
