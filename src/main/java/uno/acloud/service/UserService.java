package uno.acloud.service;

import uno.acloud.pojo.User;

public interface UserService {
    
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回用户对象，失败返回null
     */
    User login(String username, String password);
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    User findByUsername(String username);
    
    /**
     * 用户注册
     * @param user 用户对象
     * @return 注册成功返回true，失败返回false
     */
    boolean register(User user);
}
