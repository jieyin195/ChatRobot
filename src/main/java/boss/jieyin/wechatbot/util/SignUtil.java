package boss.jieyin.wechatbot.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class SignUtil {

    private String secretKey;

    // 构造器注入或setter注入都可以，这里用setter演示
    @Value("${sign.secret-key}")
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String generateSign(String timestamp, String nonce) {
        try {
            String data = timestamp + nonce;
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(secretKeySpec);
            byte[] hash = hmacSha256.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("签名生成失败", e);
        }
    }
}
