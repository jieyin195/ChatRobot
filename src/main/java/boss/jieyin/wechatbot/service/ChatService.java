package boss.jieyin.wechatbot.service;

import boss.jieyin.wechatbot.enums.MembershipLevel;
import boss.jieyin.wechatbot.mapper.ChatMessageMapper;
import boss.jieyin.wechatbot.mapper.ChatSessionMapper;
import boss.jieyin.wechatbot.mapper.UserMemberMapper;
import boss.jieyin.wechatbot.model.ChatMessage;
import boss.jieyin.wechatbot.model.ChatSession;
import boss.jieyin.wechatbot.model.AIMessage;
import boss.jieyin.wechatbot.pojo.check.MessageStatusItem;
import boss.jieyin.wechatbot.pojo.member.UserMembership;
import boss.jieyin.wechatbot.pojo.pull.PullMessage;
import boss.jieyin.wechatbot.pojo.send.*;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Content;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.springframework.ai.chat.messages.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatService {
    @Autowired
    private ChatSessionMapper chatSessionMapper;
    @Autowired private ChatMessageMapper chatMessageMapper;
    @Autowired
    private MembershipService membershipService;
    @Autowired
    private UserMemberMapper userMemberMapper;
    @Autowired
    private OpenAiService openAiService;
    @Value("${openai.api.model-name}")
    private String modelName;
    @Value("${openai.api.count}")
    private int count;

    @Autowired
    @Lazy
    private ChatService self;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private CacheService cacheService;

    private final Map<String, List<Message>> contextMap = new HashMap<>();

    private String getCurrentTimeText() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 EEEE HH:mm"));
    }

    private String buildSystemPrompt(String currentTime) {
        return String.format("""
        你是一个智能微信聊天机器人，和用户进行自然交流。当前时间是：%s。
                
        请你根据当前时间，合理应对用户关于日期、时间、节日等问题，例如“今天周几”、“现在几点”等。而是使用我提供的时间回答。
                
        其他规则：
        1. 你不会联网，不能查询天气、新闻、股票等实时信息；
        2. 回答要文明、亲切、自然，控制在200字以内；
        3. 问题不清楚时，引导用户澄清；不知道的内容请说“我暂时不确定哦”，不要编造；
        4. 禁止任何敏感、违法、低俗内容；
        5. 不使用图片、表情包或链接。
        """, currentTime);
    }
        @Transactional
    public List<PullMessage> pullPendingMessages(int limit) {
        List<ChatMessage> chatMessages = chatMessageMapper.fetchPulledMessages(limit);
        if (!chatMessages.isEmpty()) {
            List<Long> ids = chatMessages.stream().map(ChatMessage::getId).toList();
            chatMessageMapper.markMessagesAsPulled(ids);
        }
        List<PullMessage> result = new ArrayList<>();
        for (ChatMessage chat : chatMessages) {
            PullMessage msg = new PullMessage();
            msg.setMessage(chat.getModelReply());
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
        String referMsg = biz.getReferMsg().getContent();
        boolean isNewSession = (sessionId == null || sessionId.isBlank());
        if (isNewSession) {
            sessionId = UUID.randomUUID().toString(); // 自动生成新会话 ID
        }

        // 2. 取出上下文（或新建）
        List<Message> context = cacheService.getContext(userId, sessionId);
        context.add(0,new SystemMessage(buildSystemPrompt(getCurrentTimeText())));

        // 如果有引用消息，先把它加进去，格式化成辅助提示
        if (StringUtils.isNotBlank(referMsg)) {
            // 格式化引用文本，放在用户消息之前作为系统辅助内容
            String refText = "注意：用户引用了之前的一句话，内容是：\"" + referMsg.trim() + "\"。请结合这句话理解用户的新问题。";

            // 也可以用 SystemMessage 或 UserMessage 里加注释，常用做法是用 SystemMessage
            context.add(new SystemMessage(refText));
        }
        // 然后加用户的本次输入
        context.add(new UserMessage(input));
        String reply;
        try {
            Prompt prompt = new Prompt(context);
            reply = chatClient.prompt(prompt).call().content();
            // ✅ 成功后判断是否要扣次数
            UserMembership member = membershipService.getMembership(userId);
            if (member.getLevel() == MembershipLevel.NORMAL) {
                userMemberMapper.decreaseAvailableTimes(userId);
            }
        } catch (Exception e) {
            log.error("调用模型接口异常：userId={}, sessionId={}, input={}, error={}",
                    userId, sessionId, input, e.getMessage(), e);
            reply = String.format(
                    "⚠️ 很抱歉，我刚刚处理你的提问时遇到了一些问题。\n\n你提问的是：「%s」\n\n但大模型响应超时了，请稍后重试！",
                    truncate(input, 100)
            );
        }
        saveChatMessage(sessionId,reply,biz);
        // 继续后面添加 AI 回复，裁剪上下文，保存等逻辑...
        context.add(new AssistantMessage(reply));
        // 裁剪上下文逻辑省略
        // 7. 裁剪：保留 SystemMessage + 最近 N 轮（用户+助手）
        int maxTurns = 1;
        List<Message> others = context.stream().filter(m -> !(m instanceof SystemMessage)).toList();
        List<Message> recent = others.subList(Math.max(others.size() - maxTurns * 2, 0), others.size());
        context = new ArrayList<>(recent);
        // 保存上下文，返回结果...
        cacheService.updateContext(userId, sessionId, context);
        return new UserSessionInfo(userId,sessionId);
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
//    public List<AIMessage> buildChatContext(String sessionId, String input) {
//        // 1. 查询最近2轮对话
//        List<ChatMessage> history = chatMessageMapper.findRecentMessagesBySessionId(sessionId, count);
//
//        // 2. 由于数据库查的是倒序，构造上下文时要翻转顺序
//        Collections.reverse(history);
//
//        // 3. 构造上下文消息
//        List<AIMessage> context = new ArrayList<>();
//        for (ChatMessage h : history) {
//            context.add(new AIMessage("user", h.getUserInput()));
//            context.add(new AIMessage("assistant", h.getModelReply()));
//        }
//
//        // 4. 添加当前用户输入
//        context.add(new AIMessage("user", input));
//
//        return context;
//    }
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
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

}