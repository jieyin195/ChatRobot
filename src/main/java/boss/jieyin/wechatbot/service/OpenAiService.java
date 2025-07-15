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


}
