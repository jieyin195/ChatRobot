package boss.jieyin.wechatbot.pojo.member;

import lombok.Data;

import java.util.List;

@Data
public class MemberReq {
    private List<String> userIds;
    private Integer availableTimes; // 可用次数
    /**
     * 会员等级，例如1、2、3。
     *
     * 1：普通会员，记录条数
     * 2：VIP会员，优先过期时间，再条数
     * 3：超级VIP会员，无过期时间，终身免费
     */
    private Integer level;
    /**
     * 充值多少月
     */
    private Integer month;
}
