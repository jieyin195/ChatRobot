package boss.jieyin.wechatbot.service;

import boss.jieyin.wechatbot.model.AIMessage;
import boss.jieyin.wechatbot.pojo.send.ChatRequest;
import boss.jieyin.wechatbot.pojo.send.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.api.model-name}")
    private String modelName;

    private final ChatClient chatClient;

    private final RestTemplate restTemplate = new RestTemplate();

    public ChatResponse chat(List<AIMessage> aiMessages) {
        // AI例子 aiMessages参数是上下文的，模型好像优化了上下文处理，可参考博客：https://blog.csdn.net/jgk666666/article/details/147880770；
        // 或者官方文档://https://www.spring-doc.cn/spring-ai/1.0.0/api_chatclient.html
//        String content = chatClient.prompt("写提示词的地方，可以用String现在外面弄好传进来").user("用户本轮的提问").call().content();
        try {
            ChatRequest request = new ChatRequest();
            request.setModel(modelName);
            request.setMessages(aiMessages);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<ChatResponse> response = restTemplate.postForEntity(
                    apiUrl + "/chat/completions",
                    entity,
                    ChatResponse.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("调用OpenAI接口出错", e);
            throw new RuntimeException(e);
        }
    }

}
