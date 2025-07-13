package boss.jieyin.wechatbot.pojo.pull;

import lombok.Data;

import java.util.List;

@Data
public class PullMessage {
    private String message;
    private Integer msgType;
    private Integer chatType;
    private String fromUserId;
    private String toUserId;
    private List<String> at;
    private String senderTime;
    private Long msgId;
    private String sessionId;
}
