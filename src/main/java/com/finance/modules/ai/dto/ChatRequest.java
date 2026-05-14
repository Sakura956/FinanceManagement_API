package com.finance.modules.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatRequest {

    private String sessionId;

    @NotBlank(message = "消息不能为空")
    @Size(min = 1, max = 2000, message = "消息内容1-2000字")
    private String message;

    private Boolean includeHistory = true;
}
