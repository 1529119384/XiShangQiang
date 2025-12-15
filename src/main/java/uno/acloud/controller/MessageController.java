package uno.acloud.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import uno.acloud.pojo.Message;
import uno.acloud.pojo.Result;
import uno.acloud.service.MessageService;
import uno.acloud.ws.WsHub;

import java.util.List;

/**
 * 消息控制器
 */
@Slf4j
@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 用户提交消息
     */
    @PostMapping("/submit")
    public Result submitMessage(@RequestBody Message message) {
        log.info("submit message:{}", message);
        try {
            // 从请求头获取用户名
            String token = request.getHeader("token");

            Message savedMessage = messageService.submitMessage(message, token);

            log.info("save message:{}", savedMessage);
            WsHub.send2Audit(savedMessage);

            return Result.success(savedMessage);
        } catch (Exception e) {
            log.error("消息提交失败: {}", e.getMessage(), e);
            return Result.error("消息提交失败");
        }
    }

    @GetMapping("/all")
    public Result getMessages() {
        List<Message> messages = messageService.getMessages();
        return Result.success(messages);
    }

    @GetMapping("/getApprove")
    public Result getApproveMessages() {
        List<Message> messages = messageService.getApprovedMessages();
        return Result.success(messages);
    }
    @PostMapping("/approve")
    public Result approveMessage(@RequestBody List<Message> messages) {
        log.info("approve message:{}", messages);

        messageService.approveMessage(messages);

        return Result.success();
    }
    @PostMapping("/reject")
    public Result rejectMessage(@RequestBody List<Message> messages) {
        log.info("reject message:{}", messages);

        messageService.rejectMessage(messages);

        return Result.success();
    }
}