package com.example.campusbuddy.entity;

import lombok.Data;
import java.util.Date;

@Data
public class HelpApplication {
    private Long applicationId;
    private Long infoId;
    private Long applicantId;
    private Long publisherId;
    private String message;
    private String status; // PENDING, ACCEPTED, REJECTED, CANCELED
    private Date createdAt;
    private Date updatedAt;
}
