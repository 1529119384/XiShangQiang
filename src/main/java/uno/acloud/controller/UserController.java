package uno.acloud.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uno.acloud.pojo.Result;
import uno.acloud.pojo.User;
import uno.acloud.service.UserService;
import uno.acloud.utils.JwtUtils;

import java.util.Map;

@RestController
@RequestMapping("user")
public class UserController {

  @Autowired
  private UserService userService;

  /**
   * 登陆
   * 
   * @param user    提交的用户数据，包含用户名和密码
   * @return
   */
  @PostMapping("/login")
  public Result login(@RequestBody User user) {
    if (user != null && user.getUsername() != null && user.getPassword() != null) {
      // 调用service层进行登录验证
       User loginUser = userService.login(user.getUsername(), user.getPassword());
      if (loginUser != null) {
        // 生成JWT令牌
        String token = JwtUtils.generateJwt(loginUser);
        return Result.success(token);
      } else {
        return Result.error("LOGIN_FAILED", "用户名或密码错误");
      }
    } else {
      return Result.error("用户名和密码不能为空");
    }
  }

  /**
   * 获取用户名
   * 
   * @param request
   * @return
   */
  @GetMapping("/getUsername")
  public Result getUsername(HttpServletRequest request) {
    // 从请求头获取JWT令牌
    String token = request.getHeader("Authorization");
    if (token != null && token.startsWith("Bearer ")) {
      token = token.substring(7); // 移除"Bearer "前缀
      Map<String, Object> claims = JwtUtils.parseJWT(token);
      String username = (String) claims.get("username");
      return Result.success(username);
    }
    return Result.error("未授权");
  }

  /**
   * 注册
   * 
   * @param user 提交的用户数据，包含用户名和密码
   * @return
   */
  @PostMapping("/register")
  public Result register(@RequestBody User user) {
    if (user != null && user.getUsername() != null && user.getPassword() != null) {
      // 调用service层进行注册
      boolean registerResult = userService.register(user);
      if (registerResult) {
        return Result.success();
      } else {
        return Result.error("REGISTER_FAILED", "用户名已存在");
      }
    } else {
      return Result.error("用户名和密码不能为空");
    }
  }
}
