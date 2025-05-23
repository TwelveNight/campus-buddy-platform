package com.example.campusbuddy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("group_file")
public class GroupFile {
    @TableId(value = "file_id", type = IdType.AUTO)
    private Long fileId;      // 文件ID，主键
    private Long groupId;     // 小组ID
    private Long uploaderId;  // 上传者ID
    private String fileName;  // 文件名
    private String fileType;  // 文件类型，如PDF、DOCX等
    private Long fileSize;    // 文件大小(字节)
    private String fileUrl;   // 文件存储路径
    private String description; // 文件描述
    private Integer downloadCount; // 下载次数
    private String status;    // 状态：AVAILABLE、DELETED
    private Date uploadedAt;  // 上传时间
    private Date updatedAt;   // 更新时间
}
