package uno.acloud.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uno.acloud.mapper.MessageMapper;
import uno.acloud.pojo.Message;
import uno.acloud.service.MessageService;
import uno.acloud.utils.JwtUtils;
import uno.acloud.ws.WsHub;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消息服务实现类
 */
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    /**
     * 提交消息
     */
    @Override
    public Message submitMessage(Message message, String token) {
        Map<String, Object> jwtMap = JwtUtils.parseJWT(token);
        String username = (String) jwtMap.get("username");
        message.setUsername(username);
        String nickname = (String) jwtMap.get("nickname");
        message.setNickname(nickname);
        String position = (String) jwtMap.get("position");
        message.setPosition(position);
        // 设置默认值
        message.setCreateTime(LocalDateTime.now());
        message.setState(0);
        // 插入数据库
        log.debug("提交消息: {}", message);
        messageMapper.insert(message);
        log.info("消息提交成功, ID: {}", message.getId());
        return message;
    }


    /**
     * 获取已审核通过的消息列表
     */
    @Override
    public List<Message> getApprovedMessages() {
        log.debug("获取已审核通过的消息列表");
        return messageMapper.selectApprovedList();
    }


    @Override
    public List<Message> getMessages() {
        log.info("审核取得正在获取所有信息");
        List<Message> messages = messageMapper.selectMessages();
        log.info("取得的信息为{}", messages.toArray());
        return messages;
    }

    @Override
    public void approveMessage(List<Message> messages) {
        log.info("审核取得正在审核的信息为{}", messages.toArray());
        messages.forEach(message -> {
            messageMapper.approveMessage(message.getId());
            message.setState(1);
            WsHub.send2Audit(message);
            WsHub.send2Screen(message);
        });
    }

    @Override
    public void rejectMessage(List<Message> messages) {
        log.info("审核取得正在拒绝的信息为{}", messages.toArray());
        messages.forEach(message -> {
            messageMapper.rejectMessage(message.getId());
            message.setState(-1);
            WsHub.send2Audit(message);
            WsHub.send2Screen(message);
        });
    }
}