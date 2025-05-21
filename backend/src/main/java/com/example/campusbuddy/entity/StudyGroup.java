package com.example.campusbuddy.entity;

import lombok.Data;
import java.util.Date;

@Data
public class StudyGroup {
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
