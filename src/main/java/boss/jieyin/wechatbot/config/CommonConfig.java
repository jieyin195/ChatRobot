package boss.jieyin.wechatbot.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CommonConfig {
    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel){
        return ChatClient.builder(chatModel).defaultSystem("你是一个小助手，如果你不会请不要乱答").build();
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES) // 5分钟过期
                        .maximumSize(5000) // 最大缓存1000条
        );
        return cacheManager;
    }

}
