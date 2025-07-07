package boss.jieyin.wechatbot.service;

import boss.jieyin.wechatbot.model.AIMessage;
import boss.jieyin.wechatbot.pojo.send.ChatRequest;
import boss.jieyin.wechatbot.pojo.send.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.api.model-name}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();

    public ChatResponse chat(List<AIMessage> aiMessages) {
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
    }
}
