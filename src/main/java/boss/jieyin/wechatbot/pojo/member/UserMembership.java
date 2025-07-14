package boss.jieyin.wechatbot.pojo.member;

import boss.jieyin.wechatbot.enums.MembershipLevel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserMembership {
    private Long id;
    private String userId;
    private Integer memberId;
    private String type;
    private Integer status;
    private Integer availableTimes;
    private LocalDateTime expireTime;
    private MembershipLevel level;
}
