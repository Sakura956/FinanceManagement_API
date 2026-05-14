package com.finance.modules.ai.service;

import com.finance.common.result.PageResult;
import com.finance.modules.ai.dto.ChatRequest;
import com.finance.modules.ai.dto.ChatResponse;
import com.finance.modules.ai.dto.MessageVO;
import com.finance.modules.ai.dto.SessionVO;

public interface AiChatService {

    // 发送对话消息（非流式）
    ChatResponse chat(ChatRequest request);

    // 获取会话列表
    PageResult<SessionVO> getSessions(int page, int size);

    // 获取会话消息历史
    PageResult<MessageVO> getMessages(String sessionId, int page, int size);

    // 删除会话
    void deleteSession(String sessionId);
}
