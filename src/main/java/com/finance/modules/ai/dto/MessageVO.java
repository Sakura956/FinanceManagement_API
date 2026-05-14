package com.finance.modules.ai.dto;

import lombok.Data;

@Data
public class MessageVO {

    private Long id;
    private String role;
    private String content;
    private Integer tokensUsed;
    private String createTime;
}
