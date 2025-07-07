package boss.jieyin.wechatbot.pojo;

import lombok.Data;

@Data
public class ResponseEntity<T> {
    private int code;
    private String message;
    private T data;
    private String bizNo; // 新增字段：业务编号

    public ResponseEntity() {}

    public ResponseEntity(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    public ResponseEntity(int code, String message, T data, String bizNo) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.bizNo = bizNo;
    }

    // ======== 成功响应静态方法 ========
    public static <T> ResponseEntity<T> ok() {
        return new ResponseEntity<>(200, "OK", null, null);
    }

    public static <T> ResponseEntity<T> ok(T data) {
        return new ResponseEntity<>(200, "OK", data, null);
    }

    public static <T> ResponseEntity<T> ok(T data, String bizNo) {
        return new ResponseEntity<>(200, "OK", data, bizNo);
    }

    public static <T> ResponseEntity<T> ok(String message, T data, String bizNo) {
        return new ResponseEntity<>(200, message, data, bizNo);
    }
    // ======== 错误响应静态方法 ========
    public static <T> ResponseEntity<T> error(int code, String message) {
        return new ResponseEntity<>(code, message, null, null);
    }

    public static <T> ResponseEntity<T> error(String message) {
        return new ResponseEntity<>(500, message, null, null);
    }

    public static <T> ResponseEntity<T> error(String message, String bizNo) {
        return new ResponseEntity<>(500, message, null, bizNo);
    }

    public static <T> ResponseEntity<T> error(String message, T data, String bizNo) {
        return new ResponseEntity<>(500, message, data, bizNo);
    }

    public static <T> ResponseEntity<T> error(String message, T data) {
        return new ResponseEntity<>(500, message, data);
    }
}
