package com.finance.modules.statistics.dto;

import lombok.Data;

@Data
public class OverviewVO {

    private String month;
    private Double income;
    private Double expense;
    private Double balance;
}
