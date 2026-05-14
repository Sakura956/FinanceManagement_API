package com.finance.modules.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_conversation")
public class AiConversation {

    @TableId(type = IdType.AUTO)
    private Long id;//对话记录ID

    private Long userId;//用户ID

    private String sessionId;//会话ID（UUID，标识一次完整对话）

    private String role;//角色：user-用户, assistant-AI助手

    private String content;//消息内容

    private Integer tokensUsed;//消耗Token数（AI回复时记录）

    private LocalDateTime createTime;//创建时间
}
