package boss.jieyin.wechatbot.pojo.pull;

import lombok.Data;

import java.util.List;

@Data
public class PullMessageResult {
    private List<PullMessage> messageList;
    private String sessionId;
}
