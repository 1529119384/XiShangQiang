package uno.acloud.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uno.acloud.pojo.User;

@Mapper
public interface UserMapper {
    
    /**
     * 根据用户名和密码查询用户
     * @param username 用户名
     * @param password 密码
     * @return 用户对象
     */
    User findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    User findByUsername(@Param("username") String username);
    
    /**
     * 统计用户名数量，用于校验唯一性
     * @param username 用户名
     * @return 数量
     */
    int countByUsername(@Param("username") String username);
    
    /**
     * 插入用户
     * @param user 用户对象
     * @return 影响行数
     */
    int insert(User user);
}
