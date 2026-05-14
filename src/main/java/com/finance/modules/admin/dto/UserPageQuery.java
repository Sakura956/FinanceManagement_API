package com.finance.modules.admin.dto;

import lombok.Data;

/**
 * 用户查询参数 DTO
 * 封装分页查询条件
 */
@Data
public class UserPageQuery {

    private Integer page;

    private Integer size;

    private String keyword;

    private Integer status;

    private String startDate;

    private String endDate;
}
