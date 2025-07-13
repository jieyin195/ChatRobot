package boss.jieyin.wechatbot.service;

import org.springframework.ai.chat.messages.Message;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CacheService {
        @Cacheable(value = "chatContext", key = "#userId + '::' + #sessionId")
        public List<Message> getContext(String userId, String sessionId) {
            return new ArrayList<>();
        }

        @CachePut(value = "chatContext", key = "#userId + '::' + #sessionId")
        public List<Message> updateContext(String userId, String sessionId, List<Message> context) {
            return context;
        }
}
