package com.finance.modules.bill.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改账单请求 DTO
 */
@Data
public class BillUpdateRequest {

    @DecimalMin(value = "0.01", message = "金额必须大于0")
    @Digits(integer = 10, fraction = 2, message = "金额最多2位小数")
    private Double amount;

    private Long categoryId;

    @Size(max = 500, message = "备注最多500字")
    private String description;

    private String recordTime;
}
