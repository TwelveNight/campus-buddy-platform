package com.example.campusbuddy.entity;

import lombok.Data;
import java.util.Date;

@Data
public class SharedResource {
    private Long resourceId;
    private Long publisherId;
    private Long groupId;
    private String resourceName;
    private String description;
    private String fileUrl;
    private Date createdAt;
    private Date updatedAt;
}
