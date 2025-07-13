package boss.jieyin.wechatbot.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MemberEntity {
    private Integer id;
    private String name;
    private Integer level;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
}
