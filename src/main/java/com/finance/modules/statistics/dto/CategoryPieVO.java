package com.finance.modules.statistics.dto;

import lombok.Data;

import java.util.List;

@Data
public class CategoryPieVO {

    private Double totalAmount;

    private List<CategoryPieItem> items;

    @Data
    public static class CategoryPieItem {
        private Long categoryId;
        private String categoryName;
        private String categoryIcon;
        private Double amount;
        private String percent;
        private Integer count;
    }
}
