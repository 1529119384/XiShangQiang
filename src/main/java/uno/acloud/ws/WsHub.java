package uno.acloud.ws;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import uno.acloud.pojo.Message;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 纯工具类，负责：
 * 1. 保存两个身份各自的 Session 集合
 * 2. 提供统一推送 API（供业务层调用）
 * 3. JSON 序列化/关闭会话 等工具方法
 *
 * 注意：所有字段/方法均为静态，供两个 Endpoint 共享。
 */
@Slf4j
public final class WsHub {

    /* ----------  两个身份各一个集合  ---------- */
    private static final CopyOnWriteArraySet<Session> AUDIT_SET  = new CopyOnWriteArraySet<>();
    private static final CopyOnWriteArraySet<Session> SCREEN_SET = new CopyOnWriteArraySet<>();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /* ----------  推送 API（业务层直接调） ---------- */
    public static void send2Audit(Message m) {
//        String json = toJson(m);
        String json = JSON.toJSONString(m);
        AUDIT_SET.forEach(s -> s.getAsyncRemote().sendText(json));
    }

    public static void send2Screen(Message m) {
//        String json = toJson(m);
        String json = JSON.toJSONString(m);
        SCREEN_SET.forEach(s -> s.getAsyncRemote().sendText(json));
    }

    /* ----------  内部工具 ---------- */


    static void closeQuietly(Session s) {
        try { s.close(); } catch (Exception ignore) {}
    }

    /* ----------  供两个 Endpoint 分别注册/移除自己  ---------- */
    static void addAudit(Session s)   { AUDIT_SET.add(s); }
    static void removeAudit(Session s){ AUDIT_SET.remove(s); }

    static void addScreen(Session s)   { SCREEN_SET.add(s); }
    static void removeScreen(Session s){ SCREEN_SET.remove(s); }

    private WsHub() {}

    public static Integer screenSize() {
        return SCREEN_SET.size();
    }

    public static Integer auditSize() {
        return AUDIT_SET.size();
    }
}