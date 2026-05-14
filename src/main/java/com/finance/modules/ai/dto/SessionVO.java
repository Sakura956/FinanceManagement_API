package com.finance.modules.ai.dto;

import lombok.Data;

@Data
public class SessionVO {

    private String sessionId;
    private String title;
    private String lastMessage;
    private Integer messageCount;
    private String lastActiveTime;
    private String createTime;
}
