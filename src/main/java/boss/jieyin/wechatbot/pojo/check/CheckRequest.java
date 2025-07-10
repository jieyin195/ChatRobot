package boss.jieyin.wechatbot.pojo.check;

import boss.jieyin.wechatbot.pojo.User;
import lombok.Data;

import java.util.List;

@Data
public class CheckRequest {
    private User user;
    private List<MessageStatusItem> sendMessage;
}
