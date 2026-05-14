package com.finance.modules.plan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PlanUpdateRequest {

    @Size(min = 1, max = 50, message = "计划名称1-50字")
    private String name;

    //初始投入金额，修改后收益数据会重新计算
    @DecimalMin(value = "0.01", message = "初始投入金额必须大于0")
    private Double initialAmount;

    //当前市值，支持在基本信息修改时一并更新
    @DecimalMin(value = "0.00", message = "当前市值必须大于等于0")
    private Double currentValue;

    private Double expectedRoi;

    @Size(max = 500, message = "备注最多500字")
    private String remark;

    private String startDate;

    private String endDate;
}
