package uno.acloud.ws;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 审核端 WebSocket
 * 前端连接：ws://localhost:8080/ws/audit
 */
@Slf4j
@Component
@ServerEndpoint("/ws/audit")
public class AuditEndpoint {

    @OnOpen
    public void onOpen(Session session) {
        WsHub.addAudit(session);
        log.info("审核端连接: {}, 当前审核在线={}", session.getId(), WsHub.auditSize());
    }

    @OnClose
    public void onClose(Session session) {
        WsHub.removeAudit(session);
        log.info("审核端断开: {}, 剩余审核={}", session.getId(), WsHub.auditSize());
    }

    @OnError
    public void onError(Session session, Throwable e) {
        log.error("审核端异常: {}", session.getId(), e);
    }

    /* 如果前端需要心跳/应答，再写 @OnMessage 即可 */
}