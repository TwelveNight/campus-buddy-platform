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

    // 评价类型：PUBLISHER_TO_HELPER (发布者评价帮助者), HELPER_TO_PUBLISHER (帮助者评价发布者)
    private String reviewType;
}
