package com.example.campusbuddy.entity;

import lombok.Data;
import java.util.Date;

@Data
public class GroupMember {
    private Long id;
    private Long groupId;
    private Long userId;
    private String role; // MEMBER, ADMIN, CREATOR
    private String status; // ACTIVE, PENDING_APPROVAL, BANNED
    private Date joinedAt;
}
