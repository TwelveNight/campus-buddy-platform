# service/impl

存放业务逻辑接口的实现类。每个ServiceImpl类通常继承ServiceImpl<Mapper, Entity>并实现对应的Service接口，实现具体的业务逻辑。

已实现的核心ServiceImpl类：
- UserServiceImpl.java：用户业务实现。
- HelpInfoServiceImpl.java：互助信息业务实现。
- HelpApplicationServiceImpl.java：互助申请业务实现。
- StudyGroupServiceImpl.java：学习小组业务实现。
- GroupMemberServiceImpl.java：小组成员业务实现。
- SharedResourceServiceImpl.java：共享资源业务实现。
- ReviewServiceImpl.java：评价业务实现。

这些实现类可直接使用MyBatis-Plus的通用CRUD方法，也可扩展自定义业务逻辑。
