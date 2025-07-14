package boss.jieyin.wechatbot.service;

import boss.jieyin.wechatbot.pojo.member.MemberReq;
import boss.jieyin.wechatbot.pojo.member.UserMembership;

public interface MembershipService {
    UserMembership getMembership(String userId);

    void decreaseTimes(String userId);

    void insert(String userId);

    boolean modifyMember(MemberReq memberReq);
}
