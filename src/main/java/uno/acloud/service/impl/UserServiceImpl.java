package uno.acloud.service.impl;

import uno.acloud.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uno.acloud.pojo.User;
import uno.acloud.service.UserService;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public User login(String username, String password) {
        return userMapper.findByUsernameAndPassword(username, password);
    }
    
    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }
    
    @Override
    public boolean register(User user) {
        // 校验用户名是否已存在
        int count = userMapper.countByUsername(user.getUsername());
        if (count > 0) {
            return false;
        }
        // 插入用户数据，position默认为0
        if (user.getPosition() == null) {
            user.setPosition(null);
        }
        return userMapper.insert(user) > 0;
    }
}
