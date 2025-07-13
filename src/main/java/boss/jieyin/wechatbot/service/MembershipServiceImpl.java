package boss.jieyin.wechatbot.service;

import boss.jieyin.wechatbot.enums.MembershipLevel;
import boss.jieyin.wechatbot.mapper.MemberMapper;
import boss.jieyin.wechatbot.mapper.UserMemberMapper;
import boss.jieyin.wechatbot.model.MemberEntity;
import boss.jieyin.wechatbot.model.UserMemberEntity;
import boss.jieyin.wechatbot.pojo.member.UserMembership;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MembershipServiceImpl implements MembershipService {

    @Autowired
    private UserMemberMapper userMemberMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Override
    public UserMembership getMembership(String userId) {
        UserMemberEntity userMember = userMemberMapper.findByUserId(userId);
        if (userMember == null) return null;

        MemberEntity member = memberMapper.findByMemberId(userMember.getMemberId());
        if (member == null) return null;

        UserMembership result = new UserMembership();
        BeanUtils.copyProperties(userMember, result);
        result.setLevel(MembershipLevel.fromLevel(member.getLevel()));
        return result;
    }

    @Override
    public void decreaseTimes(String userId) {
        userMemberMapper.decreaseAvailableTimes(userId);
    }

    @Override
    public void insert(String userId) {
        UserMemberEntity userMember = new UserMemberEntity();
        userMember.setMemberId(MembershipLevel.NORMAL.getCode());
        userMember.setUserId(userId);
        userMember.setAvailableTimes(50);
        userMember.setType("free");
        userMemberMapper.insert(userMember);
    }
}
