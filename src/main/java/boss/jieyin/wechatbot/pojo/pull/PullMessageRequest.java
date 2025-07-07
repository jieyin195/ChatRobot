package boss.jieyin.wechatbot.pojo.pull;

import boss.jieyin.wechatbot.pojo.User;
import lombok.Data;

@Data
public class PullMessageRequest {
    private int fetchSize;
    private User user;
}
