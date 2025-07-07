package boss.jieyin.wechatbot.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private Long id;
    private String userId;
    private String sessionId;
    private Long messageId;
    private Integer messageType;
    private String userInput;
    private String modelReply;
    private String modelName;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
