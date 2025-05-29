# Campus Buddy Platform 核心功能设计文档

## 1. 实时通知系统

### 1.1 WebSocket实现架构

Campus Buddy平台的实时通知系统采用WebSocket技术实现，主要包括以下组件：

#### 前端实现
- **WebSocketService (websocket.ts)**
  - 单例模式实现的WebSocket客户端服务
  - 负责建立、维护和重连WebSocket连接
  - 提供消息发送和监听接口
  - 实现心跳机制确保连接稳定性
  - 消息去重处理，防止重复接收通知

- **NavBar WebSocket处理 (navbarWebSocket.ts)**
  - 针对导航栏组件的WebSocket功能封装
  - 处理实时通知和未读消息计数
  - 管理WebSocket连接的生命周期

#### 后端实现
- **UserWebSocketHandler**
  - 继承自Spring WebSocket的TextWebSocketHandler
  - 维护用户ID与WebSocket会话的映射关系
  - 提供针对特定用户的消息推送功能
  - 支持以下类型的通知推送：
    - 一般通知 (NOTIFICATION)
    - 私信 (PRIVATE_MESSAGE)
    - 好友申请 (FRIEND_REQUEST)
    - 好友申请状态变更 (FRIEND_REQUEST_STATUS)

### 1.2 通知系统流程

1. **连接建立**：
   - 用户登录后，前端通过WebSocketService连接到`/ws/{userId}`端点
   - 后端维护用户ID与WebSocket会话的映射

2. **通知推送**：
   - 后端通过NotificationService创建通知记录
   - 同时调用UserWebSocketHandler.sendNotification推送实时通知
   - 通知内容使用JSON格式，包含类型、标题、内容和相关链接等信息

3. **前端处理**：
   - 接收到通知后更新未读通知计数
   - 如果通知下拉菜单打开，刷新通知列表
   - 根据通知类型执行不同的处理逻辑

4. **心跳机制**：
   - 前端每60秒发送一次PING消息
   - 后端回复PONG响应以确认连接活跃
   - 如果心跳超时(10秒)，前端尝试重新连接

5. **重连策略**：
   - 使用指数退避策略，最长延迟30秒
   - 最多尝试5次重连
   - 网络恢复时自动尝试重连

## 2. 互助功能

互助功能是Campus Buddy的核心功能之一，用于促进校园内的资源共享与互助行为。

### 2.1 核心组件

- **HelpInfoController**: 互助任务管理接口
- **HelpApplicationController**: 互助申请处理接口
- **HelpInfoService**: 互助任务业务逻辑
- **HelpApplicationService**: 互助申请业务逻辑
- **NotificationService**: 集成通知服务，为互助流程各环节提供通知支持

### 2.2 互助任务状态流程

互助任务经历以下状态变化：

1. **OPEN**: 新发布的互助任务，可以接受申请
2. **IN_PROGRESS**: 申请被接受，任务正在进行中
3. **COMPLETED**: 任务已完成
4. **CANCELLED**: 任务已取消

### 2.3 互助流程

1. **发布互助任务**：
   - 用户填写并提交互助任务表单
   - 系统创建新的HelpInfo记录，状态设为OPEN
   - 可选择公开或私有模式

2. **申请互助任务**：
   - 用户浏览互助任务列表，筛选感兴趣的任务
   - 提交申请表单，系统创建HelpApplication记录，状态设为PENDING
   - 系统通过WebSocket向任务发布者发送实时通知
   - 通知内容包括申请者信息和申请的任务信息

3. **申请处理**：
   - 任务发布者查看申请列表
   - 接受申请：
     - 申请状态改为ACCEPTED
     - 任务状态改为IN_PROGRESS
     - 通过WebSocket向申请者发送接受通知
   - 拒绝申请：
     - 申请状态改为REJECTED
     - 任务仍保持OPEN状态
     - 通过WebSocket向申请者发送拒绝通知

4. **任务完成**：
   - 任务发布者或被接受的申请者标记任务为已完成
   - 系统更新任务状态为COMPLETED
   - 触发相关通知发送

5. **评价系统**：
   - 任务完成后，双方可以对对方进行评价
   - 评价提交后触发通知

## 3. 学习小组功能

学习小组功能支持用户创建或加入小组，进行讨论、文件共享等协作活动。

### 3.1 核心组件

- **GroupController**: 学习小组管理接口
- **GroupMemberService**: 小组成员管理
- **GroupPostController**: 小组讨论区帖子管理
- **GroupFileController**: 小组文件管理
- **NotificationService**: 集成通知服务，为小组活动提供通知支持

### 3.2 小组类型

- **PUBLIC**: 公开小组，任何人可以查看内容，但需申请加入才能发帖
- **PRIVATE**: 私有小组，只有成员可以查看内容和发帖
- **INVITATION**: 邀请制小组，只能通过邀请加入

### 3.3 小组功能流程

1. **创建小组**：
   - 用户填写小组信息表单(名称、描述、类型等)
   - 系统创建Group记录，创建者自动成为管理员
   - 分配专属讨论区和文件空间

2. **加入小组**：
   - 公开/私有小组：用户提交加入申请
     - 系统向小组管理员发送实时通知
     - 管理员审核后，系统发送结果通知
   - 邀请制小组：管理员发送邀请
     - 系统向被邀请者发送实时通知
     - 被邀请者接受或拒绝

3. **小组讨论区**：
   - 小组成员可发布帖子和评论
   - 支持文本、图片等多媒体内容
   - 帖子发布会更新小组动态
   - 系统缓存热门帖子提高访问效率

4. **文件共享**：
   - 成员可上传文档、图片等各类文件
   - 支持文件分类和搜索
   - 实现文件预览和下载功能
   - 文件上传与权限检查集成

5. **小组公告**：
   - 管理员可发布小组公告
   - 系统通过WebSocket向所有成员推送公告通知
   - 公告内容保存在小组信息中

## 4. 系统集成点

### 4.1 WebSocket与通知系统集成

通知系统作为核心基础设施，与互助功能和学习小组功能紧密集成：

1. **互助功能通知类型**：
   - HELP_NEW_APPLICATION: 新的互助申请
   - HELP_APPLICATION_ACCEPTED: 申请被接受
   - HELP_APPLICATION_REJECTED: 申请被拒绝
   - HELP_COMPLETED: 互助任务完成
   - HELP_NEW_REVIEW: 收到新的评价

2. **学习小组通知类型**：
   - GROUP_JOIN_APPLICATION: 新的小组加入申请
   - GROUP_JOIN_APPROVED: 小组申请已批准
   - GROUP_JOIN_REJECTED: 小组申请被拒绝
   - GROUP_INVITATION: 小组邀请
   - GROUP_ANNOUNCEMENT: 小组公告

### 4.2 通知传递流程

1. 业务层调用NotificationService创建通知记录
2. 通知服务同时通过WebSocket推送实时通知
3. 前端接收通知并更新UI
4. 用户可以通过通知快速跳转到相关页面

### 4.3 性能优化策略

1. **缓存机制**：
   - 学习小组帖子使用缓存提高访问速度
   - 热门帖子单独缓存，提高热点内容访问效率
   - 用户信息缓存减少重复查询

2. **WebSocket优化**：
   - 心跳机制保持连接稳定性
   - 消息去重防止重复处理
   - 自动重连确保通知不遗漏

3. **数据库查询优化**：
   - 分页查询减轻服务器负担
   - 条件过滤提高查询精确度
   - 批量查询减少数据库交互次数

## 5. 前后端交互模型

### 5.1 API设计原则

1. RESTful风格API设计
2. 统一响应格式(R<T>)
3. 合理的分页机制
4. 权限验证和安全防护

### 5.2 WebSocket消息格式

```json
{
  "type": "NOTIFICATION|PRIVATE_MESSAGE|FRIEND_REQUEST|...",
  "title": "通知标题",
  "content": "通知内容",
  "typeValue": "通知类型值",
  "relatedLink": "相关页面链接",
  "timestamp": 1621234567890,
  // 针对特定消息类型的附加字段
  "senderId": 123,
  "senderName": "发送者名称",
  "messageId": 456
}
```

### 5.3 错误处理

1. 前端错误处理：
   - WebSocket连接错误自动重试
   - 超时处理和友好提示
   - 断网状态检测和恢复

2. 后端错误处理：
   - 统一异常处理机制
   - 详细的错误码和信息
   - 事务保证数据一致性

## 6. 总结

Campus Buddy Platform的核心功能(互助功能、学习小组和实时通知系统)形成了一个紧密集成的生态系统。WebSocket技术为平台提供了实时通知能力，使用户能够及时接收互助申请、学习小组动态等重要信息。系统设计注重用户体验、性能优化和功能完整性，为校园互助与资源共享提供了强大的技术支持。
