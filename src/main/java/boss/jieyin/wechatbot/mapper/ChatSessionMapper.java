package boss.jieyin.wechatbot.mapper;

import boss.jieyin.wechatbot.model.ChatSession;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;


public interface ChatSessionMapper {

    /**
     * 根据 userId 查询最新的激活状态（status=1）且未删除的会话
     * @param userId 用户ID
     * @return 最近的一个激活会话，如果没有则返回 null
     */
    ChatSession findActiveSessionByUserId(@Param("userId") String userId);

    /**
     * 插入一条会话记录
     * @param chatSession 会话对象
     * @return 受影响的行数（一般为1）
     */
    int insert(ChatSession chatSession);

    void updateLastActiveTime(String sessionId, LocalDateTime lastActiveTime);

    // ChatSessionMapper.java
    int closeInactiveSessions(@Param("timeoutThreshold") LocalDateTime timeoutThreshold);

    ChatSession findBySessionIdAndUserId(@Param("sessionId") String sessionId,
                                         @Param("userId") String userId);

}
