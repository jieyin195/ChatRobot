package boss.jieyin.wechatbot.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatSession {
    private Long id;
    private String userId;
    private String sessionId;
    private String title;
    private String modelName;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
    private LocalDateTime LastActiveTime;
}
