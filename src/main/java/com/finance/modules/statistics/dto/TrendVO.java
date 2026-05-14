package com.finance.modules.statistics.dto;

import lombok.Data;

@Data
public class TrendVO {

    private String month;
    private Double income;
    private Double expense;
    private Double balance;
}
