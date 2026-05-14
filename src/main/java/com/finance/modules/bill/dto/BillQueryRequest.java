package com.finance.modules.bill.dto;

import lombok.Data;

/**
 * 账单查询请求 DTO
 */
@Data
public class BillQueryRequest {

    private Integer page;
    private Integer size;
    private Integer type;
    private Long categoryId;
    private String startDate;
    private String endDate;
    private String keyword;
    private Double minAmount;
    private Double maxAmount;
    private String sortBy;
    private String order;
}
