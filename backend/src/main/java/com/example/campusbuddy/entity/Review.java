package com.example.campusbuddy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("review")
public class Review {
    @TableId(value = "review_id", type = IdType.AUTO)
    private Long reviewId;
    private Long reviewedUserId;
    private Long reviewerUserId;
    private Long relatedInfoId;
    private Integer score;
    private String content;
    private Date createdAt;
    private String moduleType;
}
