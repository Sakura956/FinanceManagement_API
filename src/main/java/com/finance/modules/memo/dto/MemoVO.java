package com.finance.modules.memo.dto;

import lombok.Data;

@Data
public class MemoVO {

    private Long id;
    private String title;
    private String content;
    private String remindTime;
    private Integer isCompleted;
    private String createTime;
    private String updateTime;
}
