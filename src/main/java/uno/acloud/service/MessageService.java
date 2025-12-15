package uno.acloud.service;

import uno.acloud.pojo.Message;

import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService {
    /**
     * 提交消息
     */
    Message submitMessage(Message message,String token);
    
    /**
     * 获取已审核通过的消息列表
     */
    List<Message> getApprovedMessages();
    

    List<Message> getMessages();

    void approveMessage(List<Message> messages);

    void rejectMessage(List<Message> messages);
}