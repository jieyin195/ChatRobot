package boss.jieyin.wechatbot.pojo.send;

import lombok.Data;

@Data
public class ReferMsg {
    private String fromUserId;
    private String content;
    private String msgType;
}
