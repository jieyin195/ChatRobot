package boss.jieyin.wechatbot.pojo;

import lombok.Data;

@Data
public class User {
    private String userId;
    private String userName;
    private String nickName;
    private String avatar;
    private String corpId;
    private String tenantId;

    // Getters and Setters
}
