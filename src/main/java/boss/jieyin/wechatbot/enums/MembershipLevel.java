package boss.jieyin.wechatbot.enums;



public enum MembershipLevel {
    NORMAL(1),
    VIP(2),
    SUPER_VIP(3);

    private final int code;

    MembershipLevel(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MembershipLevel fromLevel(int level) {
        for (MembershipLevel l : values()) {
            if (l.getCode() == level) return l;
        }
        return NORMAL; // 默认普通会员
    }
}
