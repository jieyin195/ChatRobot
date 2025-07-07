package boss.jieyin.wechatbot.pojo.send;

import boss.jieyin.wechatbot.model.AIMessage;
import lombok.Data;

import java.util.List;

@Data
public class ChatResponse {
    private String id;
    private String model;
    private List<Choice> choices;

    @Data
    public static class Choice {
        private int index;
        private AIMessage message;
        private String finish_reason;
    }
}
