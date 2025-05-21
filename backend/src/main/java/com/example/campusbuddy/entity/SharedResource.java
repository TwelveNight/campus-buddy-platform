package com.example.campusbuddy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("shared_resource")
public class SharedResource {
    @TableId(value = "resource_id", type = IdType.AUTO)
    private Long resourceId;
    private Long publisherId;
    private Long groupId;
    private String resourceName;
    private String description;
    private String fileUrl;
    private Date createdAt;
    private Date updatedAt;
}
