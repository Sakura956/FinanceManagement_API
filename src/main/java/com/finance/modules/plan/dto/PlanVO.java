package com.finance.modules.plan.dto;

import lombok.Data;

@Data
public class PlanVO {

    private Long id;
    private String name;
    private Double initialAmount;
    private Double currentValue;
    private Double profitAmount;
    private Double profitRate;
    private Double expectedRoi;
    private String startDate;
    private String endDate;
    private Integer status;
    private String remark;
    private String createTime;
    private String updateTime;
}
