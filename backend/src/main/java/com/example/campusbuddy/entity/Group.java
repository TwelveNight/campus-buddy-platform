package com.example.campusbuddy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("study_group")  // 表名
public class Group {
    @TableId(value = "group_id", type = IdType.AUTO)
    private Long groupId;       // 小组ID，主键
    private String name;        // 小组名称
    private String description; // 小组描述
    private String avatarUrl;   // 小组头像URL
    private Long creatorId;     // 创建者ID
    private String joinType;    // 加入方式：PUBLIC(公开加入)、APPROVAL(需审批)
    private String category;    // 小组分类，如学科、兴趣、项目等
    private String tags;        // 小组标签，JSON数组字符串
    private Integer memberCount;// 成员数量
    private Integer postCount;  // 帖子数量
    private String status;      // 小组状态：ACTIVE、INACTIVE、DISBANDED
    private Date createdAt;     // 创建时间
    private Date updatedAt;     // 更新时间
}
