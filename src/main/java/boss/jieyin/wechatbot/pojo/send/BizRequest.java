package boss.jieyin.wechatbot.pojo.send;

import lombok.Data;

@Data
public class BizRequest {
    private String content;       // 内容
    private String fromUserId;    // 消息发送者的 userId
    private String toUserId;      // 消息接收者的 userId，群聊/私聊格式不同
    private String senderTime;    // 消息产生的时间
    private int chatTyp ;         // 0私聊，1群聊
    private int msgType;          // 消息类型：1文本、49引用消息
    private long msgId;           // 消息ID
    private ReferMsg referMsg;    // 被引用的消息类型
    private String sessionId;

    @Override
    public String toString() {
        return "BizRequest{" +
                "content='" + content + '\'' +
                ", fromUserId='" + fromUserId + '\'' +
                ", toUserId='" + toUserId + '\'' +
                ", senderTime='" + senderTime + '\'' +
                ", msgType=" + msgType +
                ", msgId=" + msgId +
                ", referMsg=" + referMsg +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}
