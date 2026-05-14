package com.finance.modules.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.finance.modules.ai.entity.AiConversation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

public interface AiConversationMapper extends BaseMapper<AiConversation> {

    // 查询用户会话列表（按 session_id 分组，获取每个会话的摘要信息）
    @Select("SELECT " +
            "c.session_id AS sessionId, " +
            "LEFT((SELECT a1.content FROM ai_conversation a1 WHERE a1.session_id = c.session_id AND a1.user_id = #{userId} AND a1.role = 'user' ORDER BY a1.create_time ASC LIMIT 1), 30) AS title, " +
            "(SELECT a2.content FROM ai_conversation a2 WHERE a2.session_id = c.session_id AND a2.user_id = #{userId} AND a2.role = 'user' ORDER BY a2.create_time DESC LIMIT 1) AS lastMessage, " +
            "COUNT(*) AS messageCount, " +
            "MAX(c.create_time) AS lastActiveTime, " +
            "MIN(c.create_time) AS createTime " +
            "FROM ai_conversation c " +
            "WHERE c.user_id = #{userId} " +
            "GROUP BY c.session_id " +
            "ORDER BY lastActiveTime DESC")
    IPage<Map<String, Object>> selectSessionsByUserId(Page<?> page, @Param("userId") Long userId);

    // 删除指定会话的所有消息
    @Delete("DELETE FROM ai_conversation WHERE user_id = #{userId} AND session_id = #{sessionId}")
    int deleteBySessionId(@Param("userId") Long userId, @Param("sessionId") String sessionId);
}
