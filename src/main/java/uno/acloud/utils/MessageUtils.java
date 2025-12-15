package uno.acloud.utils;

import com.alibaba.fastjson2.JSON;
import uno.acloud.ws.pojo.ResultMessage;

/**
 * 消息工具类
 */
public class MessageUtils {
    
    /**
     * 将对象转换为JSON字符串
     */
    public static String objectToJson(Object obj) {
        return JSON.toJSONString(obj);
    }
    
    /**
     * 将JSON字符串转换为对象
     */
    public static <T> T jsonToObject(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }
    
    /**
     * 创建成功响应消息
     */
    public static String createSuccessMessage(String message, Object data) {
        ResultMessage result = ResultMessage.success(message, data);
        return objectToJson(result);
    }
    
    /**
     * 创建无数据成功响应消息
     */
    public static String createSuccessMessage(String message) {
        ResultMessage result = ResultMessage.success(message);
        return objectToJson(result);
    }
    
    /**
     * 创建失败响应消息
     */
    public static String createErrorMessage(String message) {
        ResultMessage result = ResultMessage.error(message);
        return objectToJson(result);
    }
    
    /**
     * 生成唯一消息ID
     */
    public static String generateMessageId() {
        return "msg_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 1000);
    }
}
