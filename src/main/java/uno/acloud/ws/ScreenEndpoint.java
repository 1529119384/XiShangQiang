package uno.acloud.ws;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 大屏端 WebSocket
 * 前端连接：ws://localhost:8080/ws/screen
 */
@Slf4j
@Component
@ServerEndpoint("/ws/screen")
public class ScreenEndpoint {

    @OnOpen
    public void onOpen(Session session) {
        WsHub.addScreen(session);
        log.info("大屏端连接: {}, 当前大屏在线={}", session.getId(), WsHub.screenSize());
    }

    @OnClose
    public void onClose(Session session) {
        WsHub.removeScreen(session);
        log.info("大屏端断开: {}, 剩余大屏={}", session.getId(), WsHub.screenSize());
    }

    @OnError
    public void onError(Session session, Throwable e) {
        log.error("大屏端异常: {}", session.getId(), e);
    }
}