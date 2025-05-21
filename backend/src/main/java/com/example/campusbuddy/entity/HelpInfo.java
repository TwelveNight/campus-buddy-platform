package com.example.campusbuddy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("help_info")
public class HelpInfo {
    @TableId(value = "info_id", type = IdType.AUTO)
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
