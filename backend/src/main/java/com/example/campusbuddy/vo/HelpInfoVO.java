package com.example.campusbuddy.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HelpInfoVO {
    private Long infoId;
    private String title;
    private String description;
    private String type;
    private String status;
    private String reward;
    private String location;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long publisherId;
    private String publisherName;
    private String publisherAvatar;
    private Integer viewCount;
    private Integer applicationCount;
}
