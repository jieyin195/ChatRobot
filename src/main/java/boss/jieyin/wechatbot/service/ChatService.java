package boss.jieyin.wechatbot.service;

import boss.jieyin.wechatbot.mapper.ChatMessageMapper;
import boss.jieyin.wechatbot.mapper.ChatSessionMapper;
import boss.jieyin.wechatbot.model.ChatMessage;
import boss.jieyin.wechatbot.model.ChatSession;
import boss.jieyin.wechatbot.model.AIMessage;
import boss.jieyin.wechatbot.pojo.check.MessageStatusItem;
import boss.jieyin.wechatbot.pojo.pull.Message;
import boss.jieyin.wechatbot.pojo.send.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatService {
    @Autowired
    private ChatSessionMapper chatSessionMapper;
    @Autowired private ChatMessageMapper chatMessageMapper;
    @Autowired
    private OpenAiService openAiService;
    @Value("${openai.api.model-name}")
    private String modelName;
    @Value("${openai.api.count}")
    private int count;

    @Autowired
    @Lazy
    private ChatService self;

    @Transactional
    public List<Message> pullPendingMessages(int limit) {
        List<ChatMessage> chatMessages = chatMessageMapper.fetchPulledMessages(limit);
        if (!chatMessages.isEmpty()) {
            List<Long> ids = chatMessages.stream().map(ChatMessage::getId).toList();
            chatMessageMapper.markMessagesAsPulled(ids);
        }
        List<Message> result = new ArrayList<>();
        for (ChatMessage chat : chatMessages) {
            Message msg = new Message();
            msg.setMessage(chat.getUserInput());
            msg.setMsgType(chat.getMessageType());
            msg.setToUserId(chat.getUserId());
            msg.setSenderTime(String.valueOf(
                    chat.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            ));
            msg.setMsgId(chat.getMessageId());
            msg.setAt(List.of(chat.getUserId()));
            msg.setSessionId(chat.getSessionId());
            result.add(msg);
        }
        return result;
    }
    // 在 chat() 方法中调用
    // 主调方法
    public List<UserSessionInfo> chat(ReportMessageRequest request) {
        List<BizRequest> bizList = request.getBizRequest();
        if (bizList == null || bizList.isEmpty()) return Collections.emptyList();

        ExecutorService executor = Executors.newFixedThreadPool(5); // 控制并发线程池

        List<CompletableFuture<UserSessionInfo>> futures = bizList.stream()
                .map(biz -> CompletableFuture.supplyAsync(() -> self.handleBizRequest(biz), executor)
                ).toList();

        List<UserSessionInfo> result = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        executor.shutdown();
        return result;
    }

    // 子线程内执行，带事务
    @Transactional
    public UserSessionInfo handleBizRequest(BizRequest biz) {
        String userId = biz.getFromUserId();
        String input = biz.getContent();
        String sessionId = biz.getSessionId();
        ReferMsg referMsg = biz.getReferMsg();
        ChatSession session = null;

        try {
            // 获取或创建会话
            if (sessionId != null && !sessionId.isEmpty()) {
                session = findBySessionIdAndUserId(sessionId, userId);
                if (session == null) {
                    log.warn("用户 {} 尝试访问非法或过期的 sessionId: {}", userId, sessionId);
                }
            }
            if (session == null) {
                session = getOrCreateSession(userId);
            }

            updateSessionActivity(session);

            // 构建上下文
            List<AIMessage> context = buildChatContext(session.getSessionId(), input);

            // 模型调用，单线程内直接调用即可
            ChatResponse chat = openAiService.chat(context);

            if (chat == null || chat.getChoices() == null || chat.getChoices().isEmpty()) {
                log.warn("用户 {} 的模型返回为空，跳过入库", userId);
                saveChatMessage(session.getSessionId(), "", biz);
                return new UserSessionInfo(userId, session.getSessionId());
            }

            String reply = chat.getChoices().get(0).getMessage().getContent();

            // 保存消息
            saveChatMessage(session.getSessionId(), reply, biz);

            return new UserSessionInfo(userId, session.getSessionId());

        } catch (Exception e) {
            log.error("处理用户 {} 请求时出错: {}", userId, e.getMessage(), e);
            return new UserSessionInfo(userId, null);
        }
    }
    public ChatSession getOrCreateSession(String userId) {
        ChatSession session  = new ChatSession();
        session.setUserId(userId);
        session.setSessionId(UUID.randomUUID().toString());
        session.setModelName(modelName);
        session.setStatus(1);
        chatSessionMapper.insert(session);
        return session;
    }
    public ChatSession findBySessionIdAndUserId(String sessionId,String userId) {
        return chatSessionMapper.findBySessionIdAndUserId(sessionId,userId);
    }
    public void saveChatMessage(String sessionId, String reply,BizRequest bizRequest) {
        ChatMessage msg = new ChatMessage();
        msg.setUserId(bizRequest.getFromUserId());
        msg.setSessionId(sessionId);
        msg.setMessageId(bizRequest.getMsgId());
        msg.setUserInput(bizRequest.getContent());
        msg.setModelReply(reply);
        msg.setModelName(modelName);
        if(bizRequest.getMsgType()==1) {
            msg.setMessageType(bizRequest.getMsgType());
        }
        msg.setStatus(0);
        chatMessageMapper.insert(msg);
    }
    public List<AIMessage> buildChatContext(String sessionId, String input) {
        // 1. 查询最近2轮对话
        List<ChatMessage> history = chatMessageMapper.findRecentMessagesBySessionId(sessionId, count);

        // 2. 由于数据库查的是倒序，构造上下文时要翻转顺序
        Collections.reverse(history);

        // 3. 构造上下文消息
        List<AIMessage> context = new ArrayList<>();
        for (ChatMessage h : history) {
            context.add(new AIMessage("user", h.getUserInput()));
            context.add(new AIMessage("assistant", h.getModelReply()));
        }

        // 4. 添加当前用户输入
        context.add(new AIMessage("user", input));

        return context;
    }
    public void updateSessionActivity(ChatSession session) {
        session.setLastActiveTime(LocalDateTime.now());
        chatSessionMapper.updateLastActiveTime(session.getSessionId(), session.getLastActiveTime());
    }

    public void confirmMessageStatus(List<MessageStatusItem> statusList) {
        List<String> successIds = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();

        for (MessageStatusItem item : statusList) {
            if (item.isStatus()) {
                successIds.add(item.getMsgId());
            } else {
                failedIds.add(item.getMsgId());
            }
        }

        if (!successIds.isEmpty()) {
            chatMessageMapper.updateStatusByMessageIds(successIds, 2); // 成功
        }
        if (!failedIds.isEmpty()) {
            chatMessageMapper.updateStatusByMessageIds(failedIds, 3); // 失败
        }
    }

}