package boss.jieyin.wechatbot.pojo.send;

import boss.jieyin.wechatbot.pojo.User;
import lombok.Data;

import java.util.List;

@Data
public class ReportMessageRequest {
    private List<BizRequest> bizRequest;

    @Override
    public String toString() {
        return "ReportMessageRequest{" +
                ", bizRequest=" + bizRequest +
                '}';
    }
}
