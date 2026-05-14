package com.finance.modules.bill.dto;

import lombok.Data;

@Data
public class BillVO {

    private Long id;
    private Integer type;
    private Double amount;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private String description;
    private String recordTime;
    private String createTime;
    private String updateTime;
}
