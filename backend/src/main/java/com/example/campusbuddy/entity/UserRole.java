package com.example.campusbuddy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("user_role")
public class UserRole {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String roleName; // ROLE_USER, ROLE_ADMIN, ROLE_MODERATOR

    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
}
