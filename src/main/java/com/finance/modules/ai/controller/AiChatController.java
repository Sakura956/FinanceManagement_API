package com.finance.modules.ai.controller;

import com.finance.common.result.Result;
import com.finance.modules.ai.dto.ChatRequest;
import com.finance.modules.ai.service.AiChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/ai")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    // 发送对话消息
    @PostMapping("/chat")
    public Result<?> chat(@Valid @RequestBody ChatRequest request) {
        return Result.success(aiChatService.chat(request));
    }

    // 获取会话列表
    @GetMapping("/sessions")
    public Result<?> sessions(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size) {
        return Result.success(aiChatService.getSessions(page, size));
    }

    // 获取会话消息历史
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<?> messages(@PathVariable String sessionId,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "20") int size) {
        return Result.success(aiChatService.getMessages(sessionId, page, size));
    }

    // 删除会话
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(@PathVariable String sessionId) {
        aiChatService.deleteSession(sessionId);
        return Result.success("会话删除成功", null);
    }
}
