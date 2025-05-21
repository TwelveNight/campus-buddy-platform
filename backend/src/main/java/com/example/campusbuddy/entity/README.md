# entity

存放数据库实体类。每个实体类对应数据库中的一张表，用于与MyBatis-Plus等ORM框架进行数据映射。

已实现的核心实体类：
- User.java：用户表实体，包含用户基本信息、状态、信用分等。
- HelpInfo.java：互助信息表实体，描述求助/帮助信息。
- HelpApplication.java：互助申请表实体，记录用户对互助信息的响应。
- StudyGroup.java：学习小组表实体，描述小组基本信息。
- GroupMember.java：小组成员表实体，管理小组与成员的关系。
- SharedResource.java：共享资源表实体，描述学习资料、文件等资源。
- Review.java：评价表实体，记录用户互评信息。
