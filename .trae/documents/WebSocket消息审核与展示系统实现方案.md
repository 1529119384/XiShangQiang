# WebSocket消息审核与展示系统实现方案

## 1. 项目架构设计

### 1.1 核心设计理念

* **一条总线**：所有WebSocket连接使用同一端点，通过消息类型和房间标识进行路由

* **两个房间**：

  * `audit`房间：审核员接收待审核消息

  * `display-{id}`房间：大屏幕接收已审核通过的消息

### 1.2 消息流程

1. 用户通过HTTP接口提交留言
2. 后端将消息保存到数据库，状态为0（未审核）
3. 后端将消息广播到`audit`房间
4. 审核员在`audit`房间查看并审核消息
5. 审核通过后，后端更新数据库状态为1（审核通过），并将消息广播到对应`display-{id}`房间
6. 大屏幕在对应房间接收消息并渲染
7. 审核拒绝后，后端更新数据库状态为-1（审核失败）

## 2. 实现方案

### 2.1 数据模型设计

#### 2.1.1 消息状态枚举

```java
// pojo/MessageState.java
public enum MessageState {
    UNCHECKED(0, "未审核"),
    APPROVED(1, "审核通过"),
    REJECTED(-1, "审核失败");
    
    private final int code;
    private final String desc;
    // 构造函数、getter方法
}
```

#### 2.1.2 消息类型枚举

```java
// ws/pojo/MessageType.java
public enum MessageType {
    USER_MESSAGE,     // 用户提交的消息
    AUDIT_REQUEST,    // 审核请求
    AUDIT_APPROVE,    // 审核通过
    AUDIT_REJECT,     // 审核拒绝
    DISPLAY_MESSAGE   // 发送到大屏幕的消息
}
```

#### 2.1.3 消息实体类

```java
// pojo/Message.java
@Data
public class Message {
    private Integer id;             // 自增主键
    private String content;         // 消息内容，最大2000字符
    private Date createTime;        // 创建时间
    private Integer state;          // 审核状态：0-未审核，1-审核通过，-1-审核失败
    private Integer userId;         // 关联用户ID
    private String username;        // 发送者名称
    private String displayId;       // 目标大屏幕ID
}
```

#### 2.1.4 WebSocket传输消息类

```java
// ws/pojo/WebSocketMessage.java
@Data
public class WebSocketMessage {
    private String messageId;       // 消息唯一标识（用于WebSocket传输）
    private MessageType type;       // 消息类型
    private Message message;        // 消息实体
    private String roomId;          // 目标房间ID
}
```

#### 2.1.5 响应结果类

```java
// ws/pojo/ResultMessage.java
@Data
public class ResultMessage {
    private boolean success;        // 操作是否成功
    private String message;         // 响应消息
    private Object data;            // 响应数据
    private Date timestamp;         // 响应时间
}
```

### 2.2 DAO层实现

#### 2.2.1 MessageMapper接口

```java
// mapper/MessageMapper.java
@Mapper
public interface MessageMapper {
    // 新增消息
    int insert(Message message);
    
    // 根据ID更新消息
    int updateById(Message message);
    
    // 根据ID查询消息
    Message selectById(Integer id);
    
    // 查询待审核消息列表
    List<Message> selectUncheckedList();
    
    // 查询已审核通过的消息列表
    List<Message> selectApprovedList();
}
```

#### 2.2.2 MessageMapper.xml

```xml
<!-- resources/mapper/MessageMapper.xml -->
<mapper namespace="mapper.MessageMapper">
    <!-- 新增消息 -->
    <insert id="insert" parameterType="pojo.Message">
        INSERT INTO message (content, create_time, state, user_id, username, displayId)
        VALUES (#{content}, #{createTime}, #{state}, #{userId}, #{username}, #{displayId})
    </insert>
    
    <!-- 根据ID更新消息 -->
    <update id="updateById" parameterType="pojo.Message">
        UPDATE message
        <set>
            <if test="content != null">content = #{content},</if>
            <if test="createTime != null">create_time = #{createTime},</if>
            <if test="state != null">state = #{state},</if>
            <if test="userId != null">user_id = #{userId},</if>
            <if test="username != null">username = #{username},</if>
            <if test="displayId != null">displayId = #{displayId},</if>
        </set>
        WHERE id = #{id}
    </update>
    
    <!-- 根据ID查询消息 -->
    <select id="selectById" resultType="pojo.Message">
        SELECT id, content, create_time as createTime, state, user_id as userId, username, displayId
        FROM message
        WHERE id = #{id}
    </select>
    
    <!-- 查询待审核消息列表 -->
    <select id="selectUncheckedList" resultType="pojo.Message">
        SELECT id, content, create_time as createTime, state, user_id as userId, username, displayId
        FROM message
        WHERE state = 0
        ORDER BY create_time DESC
    </select>
    
    <!-- 查询已审核通过的消息列表 -->
    <select id="selectApprovedList" resultType="pojo.Message">
        SELECT id, content, create_time as createTime, state, user_id as userId, username, displayId
        FROM message
        WHERE state = 1
        ORDER BY create_time DESC
    </select>
</mapper>
```

### 2.3 服务层实现

#### 2.3.1 MessageService接口

```java
// service/MessageService.java
public interface MessageService {
    // 提交消息
    Message submitMessage(Message message);
    
    // 审核消息
    boolean auditMessage(Integer messageId, Integer state);
    
    // 根据ID查询消息
    Message getMessageById(Integer messageId);
    
    // 获取待审核消息列表
    List<Message> getUncheckedMessages();
    
    // 获取已审核通过的消息列表
    List<Message> getApprovedMessages();
}
```

#### 2.3.2 MessageServiceImpl实现

```java
// service/impl/MessageServiceImpl.java
@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private MessageMapper messageMapper;
    
    @Override
    public Message submitMessage(Message message) {
        // 设置默认值
        message.setCreateTime(new Date());
        message.setState(MessageState.UNCHECKED.getCode());
        // 插入数据库
        messageMapper.insert(message);
        return message;
    }
    
    @Override
    public boolean auditMessage(Integer messageId, Integer state) {
        Message message = new Message();
        message.setId(messageId);
        message.setState(state);
        // 更新数据库
        return messageMapper.updateById(message) > 0;
    }
    
    // 其他方法实现...
}
```

### 2.4 WebSocket端点实现

#### 2.4.1 ChatEndpoint.java 完善

* 维护房间-会话映射关系

* 实现消息路由和广播机制

* 支持房间加入和退出

#### 2.4.2 核心功能

```java
// ws/WsEndpoint.java
@ServerEndpoint(value = "/ws", configurator = GetHttpSessionConfig.class)
@Component
public class ChatEndpoint {
    // 房间-会话映射
    private static final Map<String, Set<Session>> ROOM_SESSIONS = new ConcurrentHashMap<>();
    // 会话-房间映射
    private static final Map<Session, Set<String>> SESSION_ROOMS = new ConcurrentHashMap<>();
    
    @Autowired
    private MessageService messageService;
    
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        // 连接建立时的处理
    }
    
    @OnMessage
    public void onMessage(Session session, String message) {
        // 解析WebSocketMessage
        // 根据消息类型处理
        // 1. AUDIT_APPROVE: 审核通过，更新数据库，广播到display房间
        // 2. AUDIT_REJECT: 审核拒绝，更新数据库
        // 3. 其他类型处理
    }
    
    @OnClose
    public void onClose(Session session) {
        // 连接关闭时的处理
    }
    
    // 加入房间
    private void joinRoom(Session session, String roomId) { /* 实现 */ }
    // 离开房间
    private void leaveRoom(Session session, String roomId) { /* 实现 */ }
    // 向房间广播消息
    private void broadcastToRoom(String roomId, String message) { /* 实现 */ }
    // 发送消息给特定会话
    private void sendToSession(Session session, String message) { /* 实现 */ }
}
```

### 2.5 HTTP接口实现

#### 2.5.1 消息提交接口

```java
// controller/MessageController.java
@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private ChatEndpoint chatEndpoint;
    
    @PostMapping
    public Result submitMessage(@RequestBody Message message) {
        // 保存消息到数据库
        Message savedMessage = messageService.submitMessage(message);
        // 广播到audit房间
        chatEndpoint.broadcastToRoom("audit", savedMessage);
        return Result.success("消息提交成功", savedMessage);
    }
    
    @GetMapping("/unchecked")
    public Result getUncheckedMessages() {
        List<Message> messages = messageService.getUncheckedMessages();
        return Result.success("获取待审核消息成功", messages);
    }
    
    @GetMapping("/approved")
    public Result getApprovedMessages() {
        List<Message> messages = messageService.getApprovedMessages();
        return Result.success("获取已审核消息成功", messages);
    }
}
```

### 2.6 工具类完善

#### 2.6.1 MessageUtils.java 完善

```java
// utils/MessageUtils.java
public class MessageUtils {
    // 将对象转换为JSON字符串
    public static String objectToJson(Object obj) {
        return JSON.toJSONString(obj);
    }
    
    // 将JSON字符串转换为对象
    public static <T> T jsonToObject(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }
    
    // 创建成功响应消息
    public static String createSuccessMessage(String message, Object data) {
        ResultMessage result = new ResultMessage();
        result.setSuccess(true);
        result.setMessage(message);
        result.setData(data);
        result.setTimestamp(new Date());
        return objectToJson(result);
    }
    
    // 创建失败响应消息
    public static String createErrorMessage(String message) {
        ResultMessage result = new ResultMessage();
        result.setSuccess(false);
        result.setMessage(message);
        result.setTimestamp(new Date());
        return objectToJson(result);
    }
}
```

## 3. 关键技术点

### 3.1 房间管理机制

* 使用ConcurrentHashMap确保线程安全

* 自动清理无效会话和空房间

### 3.2 消息路由逻辑

* 根据消息类型和displayId确定目标房间

* 审核消息发送到audit房间

* 审核通过后发送到对应displayId房间

### 3.3 数据库交互

* 消息持久化到MySQL数据库

* 支持消息状态更新

### 3.4 事务管理

* 审核操作使用事务确保数据一致性

## 4. 实现步骤

1. 创建消息实体类和相关枚举
2. 实现DAO层（Mapper接口和XML）
3. 实现服务层（Service接口和Impl）
4. 完善WebSocket端点实现
5. 实现HTTP接口
6. 完善消息工具类

## 5. 测试方案

1. 使用WebSocket客户端工具（如Postman、WebSocket King）测试房间连接
2. 测试用户提交消息流程
3. 测试审核员审核流程
4. 测试大屏幕接收消息流程
5. 测试多房间同时使用场景
6. 测试数据库交互和事务管理

## 6. 代码结构

```
src/main/java/
├── config/
│   ├── WebsocketConfig.java       # WebSocket配置
│   └── GetHttpSessionConfig.java  # HTTP会话获取配置
├── controller/
│   ├── UserController.java        # 现有用户控制器
│   └── MessageController.java     # 新增消息控制器
├── mapper/
│   ├── UserMapper.java            # 现有用户Mapper
│   └── MessageMapper.java         # 新增消息Mapper
├── pojo/
│   ├── Result.java                # 现有结果类
│   ├── User.java                  # 现有用户类
│   ├── Message.java               # 新增消息类
│   └── MessageState.java          # 新增消息状态枚举
├── service/
│   ├── impl/
│   │   ├── UserServiceImpl.java   # 现有用户Service实现
│   │   └── MessageServiceImpl.java # 新增消息Service实现
│   ├── UserService.java           # 现有用户Service接口
│   └── MessageService.java        # 新增消息Service接口
├── utils/
│   ├── JwtUtils.java              # 现有JWT工具
│   └── MessageUtils.java          # 完善消息工具
├── ws/
│   ├── pojo/
│   │   ├── WebSocketMessage.java  # WebSocket传输消息类
│   │   ├── MessageType.java       # WebSocket消息类型枚举
│   │   └── ResultMessage.java     # WebSocket响应结果类
│   └── ChatEndpoint.java           # 完善WebSocket端点
└── xishangqiang/
    └── XiShangQiangApplication.java
```

## 7. 注意事项

1. 确保WebSocket连接的线程安全
2. 处理好会话和房间的映射关系
3. 实现消息的可靠传输
4. 考虑系统的可扩展性，支持多个大屏幕
5. 添加必要的日志记录
6. 实现异常处理机制
7. 考虑消息的过期策略

