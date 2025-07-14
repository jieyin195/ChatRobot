package boss.jieyin.wechatbot.aspect;

import boss.jieyin.wechatbot.enums.MembershipLevel;
import boss.jieyin.wechatbot.pojo.member.UserMembership;
import boss.jieyin.wechatbot.pojo.ResponseEntity;
import boss.jieyin.wechatbot.service.MembershipService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class MembershipAccessAspect {

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
        String userId = root.path("bizRequest").get(0).path("fromUserId").asText();

        UserMembership member = membershipService.getMembership(userId);
        if(member==null){
            membershipService.insert(userId);
            return joinPoint.proceed();
        }
        if (member.getStatus() != 1) {
            return new ResponseEntity<>(403, "未开通会员或会员状态异常", null);
        }

        MembershipLevel level = member.getLevel();
        switch (level) {
            case NORMAL -> {
                if (member.getAvailableTimes() <= 0) {
                    return new ResponseEntity<>(403, "您的会员次数已用完，请升级", null);
                }
            }
            case VIP -> {
                if (member.getExpireTime() != null && member.getExpireTime().isBefore(LocalDateTime.now())) {
                    return new ResponseEntity<>(403, "您的VIP会员已过期", null);
                }
            }
            case SUPER_VIP -> {
                // 无限制，直接放行
            }
            default -> {
                return new ResponseEntity<>(403, "会员等级异常", null);
            }
        }

        return joinPoint.proceed();
    }
}
