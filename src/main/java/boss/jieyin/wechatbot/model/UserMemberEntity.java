package boss.jieyin.wechatbot.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserMemberEntity {
    private Long id;
    private String userId;
    private Integer memberId;
    private String type;
    private Integer status;
    private Integer availableTimes;
    private LocalDateTime expireTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
}

