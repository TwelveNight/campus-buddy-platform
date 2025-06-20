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

    // --- 手动补充getter/setter ---
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public String getJoinType() { return joinType; }
    public void setJoinType(String joinType) { this.joinType = joinType; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public Integer getMemberCount() { return memberCount; }
    public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }
    public Integer getPostCount() { return postCount; }
    public void setPostCount(Integer postCount) { this.postCount = postCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.util.Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.util.Date createdAt) { this.createdAt = createdAt; }
    public java.util.Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.util.Date updatedAt) { this.updatedAt = updatedAt; }
}
