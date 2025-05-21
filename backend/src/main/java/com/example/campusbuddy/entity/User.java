package com.example.campusbuddy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("user") // 添加表名注解
public class User {
    @TableId(value = "user_id", type = IdType.AUTO) // 添加主键注解
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
