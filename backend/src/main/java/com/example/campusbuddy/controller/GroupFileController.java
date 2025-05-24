package com.example.campusbuddy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.campusbuddy.common.R;
import com.example.campusbuddy.entity.GroupFile;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.service.GroupFileService;
import com.example.campusbuddy.service.UploadService;
import com.example.campusbuddy.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "学习小组文件接口", description = "学习小组文件上传、下载和管理相关操作")
@RestController
@RequestMapping("/api/group-files")
public class GroupFileController {

    @Autowired
    private GroupFileService groupFileService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private UploadService uploadService;

    @Value("${file.upload.path:${user.dir}/tmp/uploads}")
    private String uploadPath;

    /**
     * 获取当前认证用户
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.getUserByUsername(username);
    }

    /**
     * 获取小组文件列表
     */
    @Operation(summary = "获取小组文件列表", description = "支持分页和文件类型过滤")
    @GetMapping
    public R<IPage<GroupFile>> getGroupFiles(
            @Parameter(description = "学习小组ID") @RequestParam Long groupId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "文件类型") @RequestParam(required = false) String fileType) {
        
        IPage<GroupFile> files = groupFileService.queryGroupFiles(groupId, pageNum, pageSize, fileType);
        return R.ok(files);
    }

    /**
     * 获取文件详情
     */
    @Operation(summary = "获取文件详情")
    @GetMapping("/{fileId}")
    public R<GroupFile> getFileDetail(@Parameter(description = "文件ID") @PathVariable Long fileId) {
        GroupFile file = groupFileService.getFileDetail(fileId);
        if (file == null) {
            return R.fail("文件不存在");
        }
        return R.ok(file);
    }

    /**
     * 上传文件
     * @deprecated 请使用 /api/upload/group-file 接口，此接口将在未来版本移除
     */
    @Operation(summary = "上传文件到学习小组", description = "上传文件并保存到服务器，需要当前用户是小组成员。推荐使用统一的 /api/upload/group-file 接口")
    @PostMapping("/upload")
    public R<Map<String, Object>> uploadFile(
            @Parameter(description = "学习小组ID") @RequestParam Long groupId,
            @Parameter(description = "文件描述") @RequestParam(required = false) String description,
            @Parameter(description = "要上传的文件") @RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            return R.fail("请选择要上传的文件");
        }
        
        try {
            User currentUser = getCurrentUser();

            // 调用统一上传服务
            String fileUrl = uploadService.uploadGroupFile(groupId, currentUser.getUserId(), file, "group-files");

            // 保存文件信息到数据库
            Long fileId = groupFileService.uploadFile(groupId, currentUser.getUserId(), file, description);
            
            Map<String, Object> data = new HashMap<>();
            data.put("fileId", fileId);
            data.put("fileName", file.getOriginalFilename());
            data.put("fileUrl", fileUrl);

            return R.ok("文件上传成功", data);
        } catch (Exception e) {
            return R.fail("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 更新文件信息
     */
    @Operation(summary = "更新文件信息", description = "更新文件的描述等信息，仅上传者可修改")
    @PutMapping("/{fileId}")
    public R<Void> updateFileInfo(
            @Parameter(description = "文件ID") @PathVariable Long fileId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "文件信息", required = true, content = @Content(schema = @Schema(implementation = GroupFile.class)))
            @RequestBody GroupFile groupFile) {
        User currentUser = getCurrentUser();
        groupFile.setFileId(fileId);
        groupFile.setUploaderId(currentUser.getUserId());
        
        boolean success = groupFileService.updateFileInfo(groupFile);
        return success ? R.ok("文件信息更新成功", null) : R.fail("更新失败，请确认您是文件上传者");
    }

    /**
     * 删除文件
     */
    @Operation(summary = "删除文件", description = "删除文件记录并尝试删除七牛云对象，仅上传者或小组管理员可操作")
    @DeleteMapping("/{fileId}")
    public R<Void> deleteFile(@Parameter(description = "文件ID") @PathVariable Long fileId) {
        User currentUser = getCurrentUser();
        GroupFile file = groupFileService.getFileDetail(fileId);
        if (file == null) {
            return R.fail("文件不存在");
        }
        boolean success = groupFileService.deleteFile(fileId, currentUser.getUserId());
        return success ? R.ok("文件删除成功", null) : R.fail("删除失败，请确认您有权限删除该文件");
    }

    /**
     * 下载文件
     */
    @Operation(summary = "下载文件", description = "返回七牛云外链，前端可直接跳转或下载")
    @ApiResponse(responseCode = "200", description = "文件下载成功，返回重定向到七牛云外链")
    @GetMapping("/{groupId}/{fileName:.+}")
    public ResponseEntity<Void> downloadFile(
            @Parameter(description = "小组ID") @PathVariable Long groupId,
            @Parameter(description = "文件名") @PathVariable String fileName) {
        // 查找文件信息
        String keyPrefix = "group-" + groupId + "/";
        GroupFile fileInfo = null;
        for (GroupFile f : groupFileService.queryGroupFiles(groupId, 1, 100, null).getRecords()) {
            if (f.getFileUrl() != null && f.getFileUrl().contains(fileName)) {
                fileInfo = f;
                break;
            }
        }
        if (fileInfo == null) {
            return ResponseEntity.notFound().build();
        }
        groupFileService.increaseDownloadCount(fileInfo.getFileId());
        // 302重定向到七牛云外链
        return ResponseEntity.status(302).header("Location", fileInfo.getFileUrl()).build();
    }
}
