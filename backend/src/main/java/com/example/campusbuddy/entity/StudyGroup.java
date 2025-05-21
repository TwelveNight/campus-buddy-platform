package com.example.campusbuddy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("study_group")
public class StudyGroup {
    @TableId(value = "group_id", type = IdType.AUTO)
    private Long groupId;
    private Long creatorId;
    private String groupName;
    private String description;
    private String avatarUrl;
    private String joinType; // PUBLIC, APPROVAL_NEEDED
    private String groupStatus; // ACTIVE, DISBANDED
    private Integer memberCount;
    private Date createdAt;
    private Date updatedAt;
}
