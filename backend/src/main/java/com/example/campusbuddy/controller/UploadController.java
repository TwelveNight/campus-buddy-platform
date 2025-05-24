package com.example.campusbuddy.controller;

import com.example.campusbuddy.common.R;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.service.GroupFileService;
import com.example.campusbuddy.service.UploadService;
import com.example.campusbuddy.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/upload")
@Tag(name = "文件上传接口", description = "处理头像和各类文件上传")
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupFileService groupFileService;

    /**
     * 获取当前认证用户
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.getUserByUsername(username);
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/avatar")
    @Operation(summary = "上传用户头像", description = "上传用户头像图片，返回可访问的图片URL")
    public R<String> uploadAvatar(
            @Parameter(description = "头像文件", required = true) @RequestParam("file") MultipartFile file) {
        User currentUser = getCurrentUser();
        String avatarUrl = uploadService.uploadAvatar(currentUser.getUserId(), file);
        return R.ok(avatarUrl);
    }

    /**
     * 上传通用图片
     */
    @PostMapping("/image")
    @Operation(summary = "上传通用图片", description = "上传通用图片，返回可访问的图片URL")
    public R<String> uploadImage(
            @Parameter(description = "图片文件", required = true) @RequestParam("file") MultipartFile file) {
        User currentUser = getCurrentUser();
        String imageUrl = uploadService.uploadImage(currentUser.getUserId(), file);
        return R.ok(imageUrl);
    }

    /**
     * 上传学习小组文件
     */
    @PostMapping("/group-file")
    @Operation(summary = "上传学习小组文件", description = "上传学习小组文件，返回可访问的文件URL和文件ID")
    public R<Map<String, Object>> uploadGroupFile(
            @Parameter(description = "小组ID", required = true) @RequestParam("groupId") Long groupId,
            @Parameter(description = "文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件描述") @RequestParam(value = "description", required = false) String description) {
        User currentUser = getCurrentUser();
        try {
            // 上传到七牛云
            String fileUrl = uploadService.uploadGroupFile(groupId, currentUser.getUserId(), file, "group-files");
            // 保存文件信息到数据库
            Long fileId = groupFileService.uploadFile(groupId, currentUser.getUserId(), file, description);
            Map<String, Object> data = new HashMap<>();
            data.put("fileId", fileId);
            data.put("fileUrl", fileUrl);
            data.put("fileName", file.getOriginalFilename());
            return R.ok("文件上传成功", data);
        } catch (Exception e) {
            return R.fail("文件上传失败: " + e.getMessage());
        }
    }
}
