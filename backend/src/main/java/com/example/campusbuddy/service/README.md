# service

存放业务逻辑接口。每个Service接口通常继承IService<T>，用于定义与某一实体相关的业务操作。

已实现的核心Service接口：
- UserService.java：用户相关业务接口。
- HelpInfoService.java：互助信息相关业务接口。
- HelpApplicationService.java：互助申请相关业务接口。
- StudyGroupService.java：学习小组相关业务接口。
- GroupMemberService.java：小组成员相关业务接口。
- SharedResourceService.java：共享资源相关业务接口。
- ReviewService.java：评价相关业务接口。

这些接口为ServiceImpl实现类提供规范，便于后续扩展自定义业务方法。
