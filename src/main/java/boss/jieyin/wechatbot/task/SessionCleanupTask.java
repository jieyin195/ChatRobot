package boss.jieyin.wechatbot.task;

import boss.jieyin.wechatbot.mapper.ChatSessionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SessionCleanupTask {

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Scheduled(fixedRate = 60000)
    public void closeExpiredSessions() {
        // 5分钟未活跃即关闭
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(5);
        int updated = chatSessionMapper.closeInactiveSessions(timeoutThreshold);
        System.out.println("[SessionCleanup] 关闭过期会话数: " + updated);
    }
}
