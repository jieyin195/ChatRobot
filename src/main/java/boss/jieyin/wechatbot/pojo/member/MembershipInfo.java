package boss.jieyin.wechatbot.pojo.member;

import boss.jieyin.wechatbot.enums.MembershipLevel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MembershipInfo {
    private MembershipLevel type;
    private int remainingTimes; // 普通会员剩余次数
    private LocalDateTime expireTime; // VIP会员过期时间

    // 是否过期
    public boolean isExpired() {
        return expireTime != null && LocalDateTime.now().isAfter(expireTime);
    }
}

