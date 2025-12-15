package uno.acloud.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uno.acloud.pojo.User;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtils {
  // 静态变量存储配置
  private static String secretKey;
  private static Long expiration;

  // 从配置文件读取JWT密钥（静态注入）
  @Value("${jwt.secret}")
  public void setSecretKey(String secretKey) {
    JwtUtils.secretKey = secretKey;
  }

  @Value("${jwt.expiration}")
  public void setExpiration(Long expiration) {
    JwtUtils.expiration = expiration;
  }

  // 生成密钥
  private static Key getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public static String generateJwt(User u) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", u.getUserId());
    claims.put("username", u.getUsername());
    claims.put("nickname", u.getNickname());
    claims.put("position", u.getPosition());

    String jwt = Jwts.builder()
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .setClaims(claims)
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .compact();
    log.info("生成JWT成功,token:{}", jwt);
    return jwt;
  }

  public static Map<String, Object> parseJWT(String jwt) {
    Claims claims = Jwts.parser()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(jwt)
        .getBody();
    log.info("解析JWT成功,解析结果:{}", claims);
    return claims;
  }
}
