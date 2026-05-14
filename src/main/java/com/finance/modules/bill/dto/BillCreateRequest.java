package com.finance.modules.bill.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 记账请求 DTO
 */
@Data
public class BillCreateRequest {

    @NotNull(message = "账单类型不能为空")
    private Integer type;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    @Digits(integer = 10, fraction = 2, message = "金额最多2位小数")
    private Double amount;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @Size(max = 500, message = "备注最多500字")
    private String description;

    @NotNull(message = "记录时间不能为空")
    private String recordTime;
}
