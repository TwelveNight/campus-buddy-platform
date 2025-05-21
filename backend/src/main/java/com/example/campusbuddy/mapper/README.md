# mapper

存放MyBatis-Plus数据访问接口（Mapper）。每个Mapper接口对应一个实体类，继承BaseMapper<T>，用于实现对数据库表的CRUD操作。

已实现的核心Mapper接口：
- UserMapper.java：用户表数据访问接口。
- HelpInfoMapper.java：互助信息表数据访问接口。
- HelpApplicationMapper.java：互助申请表数据访问接口。
- StudyGroupMapper.java：学习小组表数据访问接口。
- GroupMemberMapper.java：小组成员表数据访问接口。
- SharedResourceMapper.java：共享资源表数据访问接口。
- ReviewMapper.java：评价表数据访问接口。

所有Mapper均已加@Mapper注解，便于Spring管理。
