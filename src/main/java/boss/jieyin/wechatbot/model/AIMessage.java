package boss.jieyin.wechatbot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AIMessage {
    private String role; // "user" or "assistant"
    private String content;
}

