package com.example.campusbuddy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("group_member")
public class GroupMember {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long groupId;
    private Long userId;
    private String role; // MEMBER, ADMIN, CREATOR
    private String status; // ACTIVE, PENDING_APPROVAL, BANNED
    private Date joinedAt;
}
