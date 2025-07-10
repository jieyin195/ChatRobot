package boss.jieyin.wechatbot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {
    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel){
        return ChatClient.builder(chatModel).defaultSystem("你是一个小助手，如果你不会请不要乱答").build();
    }

}
