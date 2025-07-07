package boss.jieyin.wechatbot.pojo.send;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSessionInfo {
    private String fromUserId;
    private String sessionId;

}
