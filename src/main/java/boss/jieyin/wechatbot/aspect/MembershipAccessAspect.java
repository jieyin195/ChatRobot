package boss.jieyin.wechatbot.aspect;

import boss.jieyin.wechatbot.enums.MembershipLevel;
import boss.jieyin.wechatbot.mapper.ChatMessageMapper;
import boss.jieyin.wechatbot.model.ChatMessage;
import boss.jieyin.wechatbot.pojo.member.UserMembership;
import boss.jieyin.wechatbot.pojo.ResponseEntity;
import boss.jieyin.wechatbot.pojo.send.BizRequest;
import boss.jieyin.wechatbot.service.ChatService;
import boss.jieyin.wechatbot.service.MembershipService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Aspect
@Component
@Slf4j
public class MembershipAccessAspect {

    @Autowired
    private ChatService chatService;

    private final MembershipService membershipService;

    public MembershipAccessAspect(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @Around("@annotation(boss.jieyin.wechatbot.annotation.CheckMembershipAccess)")
    public Object checkMembershipAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String body;
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            byte[] buf = wrapper.getContentAsByteArray();
            body = new String(buf, request.getCharacterEncoding());
        } else {
            throw new IllegalStateException("Request is not wrapped with ContentCachingRequestWrapper");
        }
        ObjectMapper mapper = new ObjectMapper();

        JsonNode root = mapper.readTree(body);
        // 读取 bizRequest 节点（数组）
        JsonNode bizRequestNode = root.path("bizRequest");
        List<BizRequest> requestList = mapper.readValue(
                bizRequestNode.traverse(), // 👈 推荐用 traverse 避免 toString 转换丢类型
                new TypeReference<List<BizRequest>>() {}
        );
        BizRequest bizRequest = requestList.get(0);
        UserMembership member = membershipService.getMembership(bizRequest.getFromUserId());
        if(member==null){
            membershipService.insert(bizRequest.getFromUserId());
            return joinPoint.proceed();
        }
        if (member.getStatus() != 1) {
            return new ResponseEntity<>(403, "账号状态异常", null);
        }

        MembershipLevel level = member.getLevel();
        switch (level) {
            case NORMAL -> {
                if (member.getAvailableTimes() <= 0) {
                    chatService.insertChatMessage(bizRequest,request,"您的可用次数已用完，请充值");
                    return new ResponseEntity<>(403, "您的可用次数已用完，请充值", null);
                }
            }
            case VIP -> {
                if (member.getExpireTime() != null && member.getExpireTime().isBefore(LocalDateTime.now())) {
                    chatService.insertChatMessage(bizRequest,request,"您的VIP已过期，请充值");
                    return new ResponseEntity<>(403, "您的VIP已过期，请充值", null);
                }
            }
            case SUPER_VIP -> {
                // 无限制，直接放行
            }
            default -> {
                return new ResponseEntity<>(403, "等级异常", null);
            }
        }

        return joinPoint.proceed();
    }
}
