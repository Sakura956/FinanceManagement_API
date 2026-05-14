package com.finance.modules.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.finance.common.exception.BusinessException;
import com.finance.common.result.PageResult;
import com.finance.modules.ai.config.AiConfig;
import com.finance.modules.ai.dto.ChatRequest;
import com.finance.modules.ai.dto.ChatResponse;
import com.finance.modules.ai.dto.MessageVO;
import com.finance.modules.ai.dto.SessionVO;
import com.finance.modules.ai.entity.AiConversation;
import com.finance.modules.ai.mapper.AiConversationMapper;
import com.finance.modules.ai.service.AiChatService;
import com.finance.util.SecurityUtil;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AiChatServiceImpl implements AiChatService {

    private final AiConversationMapper mapper;
    private final RestTemplate restTemplate;
    private final AiConfig aiConfig;

    public AiChatServiceImpl(AiConversationMapper mapper,
                             RestTemplate restTemplate,
                             AiConfig aiConfig) {
        this.mapper = mapper;
        this.restTemplate = restTemplate;
        this.aiConfig = aiConfig;
    }

    // 系统提示词：让 AI 扮演专业的财务顾问
    private static final String SYSTEM_PROMPT = "你是一个专业的个人财务顾问助手，名字叫「小财」。请用中文回答用户的问题。回答要简洁、实用、可操作。如果用户的问题与财务无关，请友好地引导回财务相关话题。";

    // 对话历史最大条数（控制在合理范围内，避免Token超限）
    private static final int MAX_HISTORY = 20;

    // ==================== 发送对话消息 ====================

    @Override
    public ChatResponse chat(ChatRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();

        // 1. 生成或使用已有会话ID
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }

        // 2. 获取历史消息（构建多轮对话上下文）
        List<AiConversation> history = new ArrayList<>();
        if (request.getIncludeHistory() != null && request.getIncludeHistory()) {
            history = mapper.selectList(
                    new LambdaQueryWrapper<AiConversation>()
                            .eq(AiConversation::getUserId, userId)
                            .eq(AiConversation::getSessionId, sessionId)
                            .orderByAsc(AiConversation::getCreateTime)
                            .last("LIMIT " + MAX_HISTORY)
            );
        }

        // 3. 保存用户消息到数据库
        AiConversation userMsg = new AiConversation();
        userMsg.setUserId(userId);
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(request.getMessage());
        userMsg.setTokensUsed(0);
        mapper.insert(userMsg);

        // 4. 构建 DeepSeek API 请求消息列表
        List<Map<String, String>> messages = new ArrayList<>();
        // 系统提示词
        Map<String, String> sysMsg = new HashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content", SYSTEM_PROMPT);
        messages.add(sysMsg);
        // 历史对话
        for (AiConversation h : history) {
            Map<String, String> m = new HashMap<>();
            m.put("role", h.getRole());
            m.put("content", h.getContent());
            messages.add(m);
        }
        // 当前用户消息
        Map<String, String> curMsg = new HashMap<>();
        curMsg.put("role", "user");
        curMsg.put("content", request.getMessage());
        messages.add(curMsg);

        // 5. 构建请求体并调用 DeepSeek API
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiConfig.getModel());
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", aiConfig.getMaxTokens());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + aiConfig.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        String url = aiConfig.getBaseUrl() + "/chat/completions";

        Map<String, Object> responseBody;
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            responseBody = response.getBody();
        } catch (Exception e) {
            throw new BusinessException(500, "AI服务调用失败，请稍后重试: " + e.getMessage());
        }

        // 6. 解析 API 响应
        String aiContent;
        int tokensUsed;
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new BusinessException(500, "AI未返回有效回复");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> choiceMsg = (Map<String, Object>) choices.get(0).get("message");
            aiContent = (String) choiceMsg.get("content");

            @SuppressWarnings("unchecked")
            Map<String, Object> usage = (Map<String, Object>) responseBody.get("usage");
            tokensUsed = (usage != null && usage.get("total_tokens") != null)
                    ? ((Number) usage.get("total_tokens")).intValue()
                    : 0;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(500, "AI响应解析失败");
        }

        // 7. 保存 AI 回复到数据库
        AiConversation aiMsg = new AiConversation();
        aiMsg.setUserId(userId);
        aiMsg.setSessionId(sessionId);
        aiMsg.setRole("assistant");
        aiMsg.setContent(aiContent);
        aiMsg.setTokensUsed(tokensUsed);
        mapper.insert(aiMsg);

        // 8. 构建返回结果
        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setSessionId(sessionId);
        chatResponse.setMessage(aiContent);
        chatResponse.setTokensUsed(tokensUsed);
        chatResponse.setCreatedAt(formatDateTime(LocalDateTime.now()));

        return chatResponse;
    }

    // ==================== 获取会话列表 ====================

    @Override
    public PageResult<SessionVO> getSessions(int page, int size) {
        Long userId = SecurityUtil.getCurrentUserId();

        Page<Map<String, Object>> pageObj = new Page<>(page, size);
        IPage<Map<String, Object>> result = mapper.selectSessionsByUserId(pageObj, userId);

        List<SessionVO> records = new ArrayList<>();
        for (Map<String, Object> row : result.getRecords()) {
            SessionVO vo = new SessionVO();
            vo.setSessionId((String) row.get("sessionId"));
            // title 取第一条用户消息前30字，没有则默认"新对话"
            String title = (String) row.get("title");
            vo.setTitle(title != null ? title : "新对话");
            String lastMessage = (String) row.get("lastMessage");
            vo.setLastMessage(lastMessage != null ? lastMessage : "");
            vo.setMessageCount(((Number) row.get("messageCount")).intValue());
            vo.setLastActiveTime(formatDateTime(row.get("lastActiveTime")));
            vo.setCreateTime(formatDateTime(row.get("createTime")));
            records.add(vo);
        }

        return new PageResult<>(result.getTotal(), page, size, records);
    }

    // ==================== 获取会话消息历史 ====================

    @Override
    public PageResult<MessageVO> getMessages(String sessionId, int page, int size) {
        Long userId = SecurityUtil.getCurrentUserId();

        // 校验该会话是否属于当前用户（查询是否有记录）
        Long count = mapper.selectCount(
                new LambdaQueryWrapper<AiConversation>()
                        .eq(AiConversation::getUserId, userId)
                        .eq(AiConversation::getSessionId, sessionId)
        );
        if (count == 0) {
            throw new BusinessException(404, "会话不存在");
        }

        // 按创建时间升序获取消息
        Page<AiConversation> pageObj = new Page<>(page, size);
        IPage<AiConversation> result = mapper.selectPage(pageObj,
                new LambdaQueryWrapper<AiConversation>()
                        .eq(AiConversation::getUserId, userId)
                        .eq(AiConversation::getSessionId, sessionId)
                        .orderByAsc(AiConversation::getCreateTime)
        );

        List<MessageVO> records = new ArrayList<>();
        for (AiConversation conv : result.getRecords()) {
            MessageVO vo = new MessageVO();
            vo.setId(conv.getId());
            vo.setRole(conv.getRole());
            vo.setContent(conv.getContent());
            vo.setTokensUsed(conv.getTokensUsed());
            vo.setCreateTime(formatDateTime(conv.getCreateTime()));
            records.add(vo);
        }

        return new PageResult<>(result.getTotal(), page, size, records);
    }

    // ==================== 删除会话 ====================

    @Override
    public void deleteSession(String sessionId) {
        Long userId = SecurityUtil.getCurrentUserId();

        // 校验该会话是否属于当前用户
        Long count = mapper.selectCount(
                new LambdaQueryWrapper<AiConversation>()
                        .eq(AiConversation::getUserId, userId)
                        .eq(AiConversation::getSessionId, sessionId)
        );
        if (count == 0) {
            throw new BusinessException(404, "会话不存在");
        }

        mapper.deleteBySessionId(userId, sessionId);
    }

    // ==================== 工具方法 ====================

    /**
     * 统一格式化日期时间（兼容 LocalDateTime 和 Timestamp）
     */
    private String formatDateTime(Object timeObj) {
        if (timeObj == null) return null;
        if (timeObj instanceof LocalDateTime) {
            return ((LocalDateTime) timeObj).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (timeObj instanceof Timestamp) {
            return ((Timestamp) timeObj).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        // 兜底：直接转字符串
        String str = timeObj.toString();
        // 如果格式是 "2026-05-10T15:30:00"，替换 T 为空格
        if (str.contains("T")) {
            str = str.replace("T", " ").substring(0, 19);
        }
        return str;
    }
}
