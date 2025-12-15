package uno.acloud.config;

import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.context.annotation.Configuration;
import uno.acloud.utils.JwtUtils;



import java.util.List;
import java.util.Map;

@Configuration
public class GetHttpSessionConfig extends ServerEndpointConfig.Configurator {

  @Override
  public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
    // 从请求参数获取JWT令牌
    Map<String, Object> userProperties = sec.getUserProperties();
    Map<String, List<String>> parameters = request.getParameterMap();
    if (parameters.containsKey("token")) {
      List<String> tokenList = parameters.get("token");
      if (tokenList != null && !tokenList.isEmpty()) {
        String token = tokenList.get(0);
        try {
          // 解析JWT令牌
          Map<String, Object> claims = JwtUtils.parseJWT(token);
          // 将用户信息存储到属性中
          userProperties.put("userId", claims.get("userId"));
          userProperties.put("username", claims.get("username"));
          userProperties.put("position", claims.get("position"));
        } catch (Exception e) {
          // JWT解析失败，忽略
        }
      }
    }
  }
}
