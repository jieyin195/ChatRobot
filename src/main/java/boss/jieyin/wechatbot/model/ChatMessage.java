package boss.jieyin.wechatbot.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private Long id;
    private String userId;
    private String groupId;
    private String sessionId;
    private String robotId;
    private Long messageId;
    private Integer chatType;
    private Integer messageType;
    private String userInput;
    private String modelReply;
    private String modelName;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
