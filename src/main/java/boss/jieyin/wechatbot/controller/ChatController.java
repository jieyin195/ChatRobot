package boss.jieyin.wechatbot.controller;

import boss.jieyin.wechatbot.annotation.CheckMembershipAccess;
import boss.jieyin.wechatbot.pojo.ResponseEntity;
import boss.jieyin.wechatbot.pojo.check.CheckRequest;
import boss.jieyin.wechatbot.pojo.pull.PullMessage;
import boss.jieyin.wechatbot.pojo.send.ReportMessageRequest;
import boss.jieyin.wechatbot.pojo.send.UserSessionInfo;
import boss.jieyin.wechatbot.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/chat/v1")
@Slf4j
//@CheckSign
public class ChatController {
    @Autowired
    private ChatService chatService;

    @PostMapping("/send")
    @CheckMembershipAccess
    public ResponseEntity<List<UserSessionInfo>> chat(@RequestBody ReportMessageRequest reportMessageRequest, HttpServletRequest request) {
        // 聊天上传
        String bizNo = request.getHeader("bizNo");
        log.info("send此次请求的单号:{},\n\n请求体:{}",bizNo,reportMessageRequest);
        List<UserSessionInfo> chat = chatService.chat(reportMessageRequest,request);
        return ResponseEntity.ok(chat, bizNo);
    }

    @PostMapping("/pull")
    public ResponseEntity<List<PullMessage>> pull(HttpServletRequest request) {
        log.info("pull此次请求的单号:{}",request.getHeader("bizNo"));
        String robotId = request.getHeader("robotId");
        List<PullMessage> messages = chatService.pullPendingMessages(2,robotId);
        return ResponseEntity.ok(messages,request.getHeader("bizNo"));
    }

    @PostMapping("/confirm-status")
    public ResponseEntity<String> confirmStatus(@RequestBody CheckRequest checkRequest,HttpServletRequest request) {
        log.info("confirm-status此次请求的单号:{}",request.getHeader("bizNo"));
        chatService.confirmMessageStatus(checkRequest.getSendMessage());
        return ResponseEntity.ok("状态已更新",request.getHeader("bizNo"));
    }

}