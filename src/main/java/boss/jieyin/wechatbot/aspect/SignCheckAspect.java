package boss.jieyin.wechatbot.aspect;

import boss.jieyin.wechatbot.annotation.CheckSign;
import boss.jieyin.wechatbot.pojo.ResponseEntity;
import boss.jieyin.wechatbot.util.SignUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class SignCheckAspect {
    private final SignUtil signUtil;

    public SignCheckAspect(SignUtil signUtil) {
        this.signUtil = signUtil;
    }

    @Pointcut("@annotation(boss.jieyin.wechatbot.annotation.CheckSign)||@within(boss.jieyin.wechatbot.annotation.CheckSign)")
    public void checkSignPointcut() {}

    @Around("checkSignPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 方法上查
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CheckSign checkSign = method.getAnnotation(CheckSign.class);

        // 方法上没有，就类上查
        if (checkSign == null) {
            Class<?> clazz = joinPoint.getTarget().getClass();
            checkSign = clazz.getAnnotation(CheckSign.class);
        }

        // 没有注解或 value = false，直接放行
        if (checkSign == null || !checkSign.value()) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String timestamp = request.getHeader("timestamp");
        String nonce = request.getHeader("nonce");
        String sign = request.getHeader("sign");

        // 1. 检查参数
        if (StringUtils.isAnyBlank(timestamp, nonce, sign)) {
            return new ResponseEntity<>(401, "签名无效", null);
        }

        // 2. 校验时间戳是否超时（5 分钟）
        long ts;
        try {
            ts = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(401, "非法时间戳", null);
        }

        long now = System.currentTimeMillis();
        if (Math.abs(now - ts) > 5 * 60 * 1000) {
            return new ResponseEntity<>(401, "签名过期", null);
        }

        // 3. 校验签名
        String expectedSign = signUtil.generateSign(timestamp, nonce);
        if (!expectedSign.equals(sign)) {
            return new ResponseEntity<>(401, "签名无效", null);
        }

        // 放行
        return joinPoint.proceed();
    }
}
