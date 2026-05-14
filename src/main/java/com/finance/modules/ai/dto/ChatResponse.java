package com.finance.modules.ai.dto;

import lombok.Data;

@Data
public class ChatResponse {

    private String sessionId;
    private String message;
    private Integer tokensUsed;
    private String createdAt;
}
