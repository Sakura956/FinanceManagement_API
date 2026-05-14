package com.finance.modules.plan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PlanCreateRequest {

    @NotBlank(message = "计划名称不能为空")
    @Size(min = 1, max = 50, message = "计划名称1-50字")
    private String name;

    @NotNull(message = "初始投入金额不能为空")
    @DecimalMin(value = "0.01", message = "初始投入金额必须大于0")
    private Double initialAmount;

    @NotNull(message = "当前市值不能为空")
    @DecimalMin(value = "0.00", message = "当前市值必须大于等于0")
    private Double currentValue;

    private Double expectedRoi;

    @NotNull(message = "开始日期不能为空")
    private String startDate;

    private String endDate;

    @Size(max = 500, message = "备注最多500字")
    private String remark;
}
