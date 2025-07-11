package api;

import boss.jieyin.wechatbot.model.AIMessage;
import boss.jieyin.wechatbot.pojo.send.ChatRequest;
import boss.jieyin.wechatbot.pojo.send.ChatResponse;

import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class PromptTest {

    private static final String apiKey = "";         // 补充自己的 apiKey 进行调试
    private static final String apiUrl = "https://api.openai-hk.com/v1/chat/completions";
    private static final String modelName = "gpt-4o-mini";

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    public void chat() {
        List<AIMessage> aiMessages = getAiMessages();

        try {
            ChatRequest request = new ChatRequest();
            request.setModel(modelName);
            request.setMessages(aiMessages);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<ChatResponse> response = restTemplate.postForEntity(apiUrl, entity, ChatResponse.class);

            System.out.println(response.getBody());
            System.out.println("时间戳：" + System.currentTimeMillis());       // 因为没联网，所以AI的回复无法带上日期，但是可以通过回复完成后的时间戳，通过拼接来追加在对话信息里
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<AIMessage> getAiMessages() {
        List<AIMessage> aiMessages = new ArrayList<>();
        aiMessages.add(new AIMessage("system", getSystemPrompt()));
        aiMessages.add(new AIMessage("user", "雷军在小米的职位是什么"));
        aiMessages.add(new AIMessage("assistant", "雷军在小米的职位是创始人、董事长兼首席执行官（CEO）。如果你还有其他问题，欢迎随时问我！"));
        aiMessages.add(new AIMessage("user", "龙绍松是谁呢？"));
        return aiMessages;
    }

    private String getSystemPrompt() {
        return "假设你是一个智能微信对话机器人，你将帮忙用户解决一些日常的问题，以及与用户进行聊天对话。" +                     // 首先给 AI 设定一个角色。比如我们的功能是微信对话机器人
                "解决的额问题主要包括微信的一些常用功能使用，以及一些生活常识。" +                                         // 然后给 AI 设定需要完成的目标，并详细说明
                "聊天的内容请限定在正常的范围内，聊天的态度要文明、礼貌且富含亲和力。" +
                "请注意，对于用户提问的内容，如果能清晰说明，则进行正常解答；" +
                    "如果不清晰用户提出的问题，可以对用户的问题进行补充提问作为回复，或者直接回答不知道，请勿回复无关的内容。" +   // 补充说明，对回答的内容做限制
//                "回复的格式请参考：xxxx" +
                "请将回复的内容限制在100汉字以内。";
    }
}

