package com.example.campusbuddy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.campusbuddy.common.R;
import com.example.campusbuddy.entity.GroupFile;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.service.GroupFileService;
import com.example.campusbuddy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 学习小组文件控制器
 */
@RestController
@RequestMapping("/api/group-files")
public class GroupFileController {

    @Autowired
    private GroupFileService groupFileService;

    @Autowired
    private UserService userService;
    
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
    @GetMapping
    public R<IPage<GroupFile>> getGroupFiles(
            @RequestParam Long groupId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String fileType) {
        
        IPage<GroupFile> files = groupFileService.queryGroupFiles(groupId, pageNum, pageSize, fileType);
        return R.ok(files);
    }

    /**
     * 获取文件详情
     */
    @GetMapping("/{fileId}")
    public R<GroupFile> getFileDetail(@PathVariable Long fileId) {
        GroupFile file = groupFileService.getFileDetail(fileId);
        if (file == null) {
            return R.fail("文件不存在");
        }
        return R.ok(file);
    }

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public R<Map<String, Object>> uploadFile(
            @RequestParam Long groupId,
            @RequestParam(required = false) String description,
            @RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            return R.fail("请选择要上传的文件");
        }
        
        try {
            User currentUser = getCurrentUser();
            Long fileId = groupFileService.uploadFile(groupId, currentUser.getUserId(), file, description);
            
            Map<String, Object> data = new HashMap<>();
            data.put("fileId", fileId);
            data.put("fileName", file.getOriginalFilename());
            
            return R.ok("文件上传成功", data);
        } catch (Exception e) {
            return R.fail("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 更新文件信息
     */
    @PutMapping("/{fileId}")
    public R<Void> updateFileInfo(@PathVariable Long fileId, @RequestBody GroupFile groupFile) {
        User currentUser = getCurrentUser();
        groupFile.setFileId(fileId);
        groupFile.setUploaderId(currentUser.getUserId());
        
        boolean success = groupFileService.updateFileInfo(groupFile);
        return success ? R.ok("文件信息更新成功", null) : R.fail("更新失败，请确认您是文件上传者");
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{fileId}")
    public R<Void> deleteFile(@PathVariable Long fileId) {
        User currentUser = getCurrentUser();
        GroupFile file = groupFileService.getFileDetail(fileId);
        
        if (file == null) {
            return R.fail("文件不存在");
        }
        
        boolean success = groupFileService.deleteFile(fileId, currentUser.getUserId());
        if (success) {
            // 尝试物理删除文件（如果存在）
            try {
                // 从文件URL中提取文件名
                String fileUrl = file.getFileUrl(); 
                String[] parts = fileUrl.split("/");
                String fileName = parts[parts.length - 1];
                
                // 构建文件路径并尝试删除
                Path filePath = groupFileService.getPhysicalFilePath(file.getGroupId(), fileName);
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            } catch (Exception e) {
                // 记录错误但不影响逻辑删除结果
                e.printStackTrace();
            }
            
            return R.ok("文件删除成功", null);
        } else {
            return R.fail("删除失败，请确认您有权限删除该文件");
        }
    }

    /**
     * 下载文件
     */
    @GetMapping("/{groupId}/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long groupId, @PathVariable String fileName) {
        try {
            // 获取文件物理路径
            Path filePath = groupFileService.getPhysicalFilePath(groupId, fileName);
            Resource resource = new UrlResource(filePath.toUri());
            
            // 检查文件是否存在
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            // 查找文件信息
            String fileUrl = "/api/group-files/" + groupId + "/" + fileName;
            GroupFile fileInfo = groupFileService.getByFileUrl(fileUrl);
            
            // 如果找到了文件记录，更新下载计数
            if (fileInfo != null) {
                groupFileService.increaseDownloadCount(fileInfo.getFileId());
            }
            
            // 获取文件原始名称（如果有文件信息，使用原始文件名，否则使用当前文件名）
            String originalFilename = (fileInfo != null) ? fileInfo.getFileName() : fileName;
            
            // 获取文件的MIME类型
            String contentType = Files.probeContentType(filePath);
            MediaType mediaType = (contentType != null) ? 
                MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM;
            
            // 构建响应头
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFilename + "\"")
                    .body(resource);
            
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
