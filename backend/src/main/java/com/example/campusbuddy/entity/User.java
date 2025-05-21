package com.example.campusbuddy.entity;

import lombok.Data;
import java.util.Date;

@Data
public class User {
    private Long userId;
    private String username;
    private String passwordHash;
    private String nickname;
    private String avatarUrl;
    private String gender; // MALE, FEMALE, UNKNOWN
    private String major;
    private String grade;
    private String contactInfo;
    private String skillTags; // JSON字符串
    private Integer creditScore;
    private String status; // ACTIVE, INACTIVE, BANNED
    private Date createdAt;
    private Date updatedAt;
}
