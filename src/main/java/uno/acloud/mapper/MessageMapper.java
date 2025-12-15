package uno.acloud.mapper;

import org.apache.ibatis.annotations.Mapper;
import uno.acloud.pojo.Message;

import java.util.List;

/**
 * 消息Mapper接口
 */
@Mapper
public interface MessageMapper {
    /**
     * 新增消息
     */
    int insert(Message message);

    /**
     * 查询已审核通过的消息列表
     */
    List<Message> selectApprovedList();
    
    List<Message> selectMessages();

    void approveMessage(Integer id);

    void rejectMessage(Integer id);
}