package uno.acloud.ws.pojo;

import lombok.Data;

import java.util.Date;

/**
 * 用来封装服务端给浏览器发送的消息数据
 */
@Data
public class ResultMessage {
    private boolean success;          // 操作是否成功
    private String message;           // 响应消息
    private Object data;              // 响应数据
    private Date timestamp;           // 响应时间
    
    /**
     * 成功响应构造方法
     */
    public ResultMessage(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = new Date();
    }
    
    /**
     * 无数据成功响应构造方法
     */
    public ResultMessage(boolean success, String message) {
        this(success, message, null);
    }
    
    /**
     * 默认构造方法
     */
    public ResultMessage() {
        this.timestamp = new Date();
    }
    
    /**
     * 创建成功响应
     */
    public static ResultMessage success(String message, Object data) {
        return new ResultMessage(true, message, data);
    }
    
    /**
     * 创建无数据成功响应
     */
    public static ResultMessage success(String message) {
        return new ResultMessage(true, message);
    }
    
    /**
     * 创建失败响应
     */
    public static ResultMessage error(String message) {
        return new ResultMessage(false, message);
    }
}
