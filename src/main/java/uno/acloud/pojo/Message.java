package uno.acloud.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息实体类
 */
@Data
public class Message {

    private Integer id;

    private String content;

    private LocalDateTime createTime;

    private Integer state;

    private Integer userId;

    private String username;

    private String nickname;

    private String position;

    private String displayId;



}