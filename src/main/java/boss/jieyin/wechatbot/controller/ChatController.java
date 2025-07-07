package boss.jieyin.wechatbot.controller;

import boss.jieyin.wechatbot.annotation.CheckSign;
import boss.jieyin.wechatbot.pojo.ResponseEntity;
import boss.jieyin.wechatbot.pojo.check.MessageStatusItem;
import boss.jieyin.wechatbot.pojo.pull.Message;
import boss.jieyin.wechatbot.pojo.send.ReportMessageRequest;
import boss.jieyin.wechatbot.pojo.send.UserSessionInfo;
import boss.jieyin.wechatbot.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/chat/v1")
@Slf4j
public class ChatController {
    @Autowired
    private ChatService chatService;

    @PostMapping("/send")
    @CheckSign(value = false)
    public ResponseEntity<List<UserSessionInfo>> chat(@RequestBody ReportMessageRequest reportMessageRequest) {
        // 用户存储和会员制验证

        // 聊天上传
        String bizNo = UUID.randomUUID().toString();
        log.debug("此次请求的单号:{},\n\n请求体:{}",bizNo,reportMessageRequest);
        List<UserSessionInfo> chat = chatService.chat(reportMessageRequest);
        return ResponseEntity.ok(chat, bizNo);
    }

    @PostMapping("/pull")
    public ResponseEntity<List<Message>> pull() {
        String bizNo = UUID.randomUUID().toString();
        log.debug("此次请求的单号:{}",bizNo);
        List<Message> messages = chatService.pullPendingMessages(2);
        return ResponseEntity.ok(messages,bizNo);
    }

    @PostMapping("/confirm-status")
    public ResponseEntity<String> confirmStatus(@RequestBody List<MessageStatusItem> statusList) {
        chatService.confirmMessageStatus(statusList);
        return ResponseEntity.ok("状态已更新");
    }

}