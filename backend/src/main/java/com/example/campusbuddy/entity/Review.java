package com.example.campusbuddy.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Review {
    private Long reviewId;
    private Long reviewedUserId;
    private Long reviewerUserId;
    private Long relatedInfoId;
    private Integer score;
    private String content;
    private Date createdAt;
}
