package boss.jieyin.wechatbot.pojo.send;

import boss.jieyin.wechatbot.model.AIMessage;
import lombok.Data;

import java.util.List;


@Data
public class ChatRequest {
    private String model;
    private List<AIMessage> messages;
    private Double temperature;
    private Boolean stream;
}
