package com.example.campusbuddy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long acceptedApplicationId; // 已接受的申请ID

    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
    
    // 非数据库字段，存储临时参数
    @TableField(exist = false)
    private Map<String, Object> params = new HashMap<>();
}
