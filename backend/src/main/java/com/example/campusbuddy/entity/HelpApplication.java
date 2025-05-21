package com.example.campusbuddy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("help_application")
public class HelpApplication {
    @TableId(value = "application_id", type = IdType.AUTO)
    private Long applicationId;
    private Long infoId;
    private Long applicantId;
    private Long publisherId;
    private String message;
    private String status; // PENDING, ACCEPTED, REJECTED, CANCELED
    private Date createdAt;
    private Date updatedAt;
}
