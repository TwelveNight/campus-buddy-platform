package com.example.campusbuddy.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class HelpInfo {
    private Long infoId;
    private Long publisherId;
    private String type; // COURSE_TUTORING, SKILL_LEARNING, ITEM_LEND, ITEM_EXCHANGE, TEAM_UP
    private String title;
    private String description;
    private String expectedTime;
    private String expectedLocation;
    private String contactMethod;
    private BigDecimal rewardAmount;
    private String imageUrls; // JSON字符串
    private String status; // OPEN, IN_PROGRESS, RESOLVED, EXPIRED, CLOSED
    private Integer viewCount;
    private Date createdAt;
    private Date updatedAt;
}
