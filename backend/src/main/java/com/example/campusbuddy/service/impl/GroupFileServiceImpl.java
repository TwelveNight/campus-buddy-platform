package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.entity.GroupFile;
import com.example.campusbuddy.entity.GroupMember;
import com.example.campusbuddy.mapper.GroupFileMapper;
import com.example.campusbuddy.mapper.GroupMemberMapper;
import com.example.campusbuddy.service.GroupFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;

/**
 * 学习小组文件服务实现类
 */
@Service
public class GroupFileServiceImpl extends ServiceImpl<GroupFileMapper, GroupFile> implements GroupFileService {

    @Autowired
    private GroupMemberMapper groupMemberMapper;
    
    @Value("${file.upload.path:${user.dir}/tmp/uploads}")
    private String uploadPath;

    @Override
    public IPage<GroupFile> queryGroupFiles(Long groupId, Integer pageNum, Integer pageSize, String fileType) {
        LambdaQueryWrapper<GroupFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupFile::getGroupId, groupId)
                .eq(GroupFile::getStatus, "AVAILABLE");
        
        // 添加文件类型过滤
        if (StringUtils.hasText(fileType)) {
            queryWrapper.eq(GroupFile::getFileType, fileType);
        }
        
        // 按上传时间倒序排序
        queryWrapper.orderByDesc(GroupFile::getUploadedAt);
        
        return page(new Page<>(pageNum, pageSize), queryWrapper);
    }

    @Override
    @Transactional
    public Long uploadFile(Long groupId, Long uploaderId, MultipartFile file, String description) {
        try {
            // 确保上传路径存在
            File uploadDir = new File(uploadPath + "/group-files/" + groupId);
            if (!uploadDir.exists()) {
                boolean dirCreated = uploadDir.mkdirs();
                if (!dirCreated) {
                    throw new RuntimeException("无法创建文件目录：" + uploadDir.getAbsolutePath());
                }
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "unnamed-file";
            }
            
            String fileExtension = "";
            if (originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            
            // 保存文件
            Path filePath = Paths.get(uploadDir.getAbsolutePath(), uniqueFileName);
            Files.write(filePath, file.getBytes());
            
            // 保存文件信息
            GroupFile groupFile = new GroupFile();
            groupFile.setGroupId(groupId);
            groupFile.setUploaderId(uploaderId);
            groupFile.setFileName(originalFilename);
            groupFile.setFileType(fileExtension.isEmpty() ? "UNKNOWN" : fileExtension.substring(1).toUpperCase()); // 去掉点号，转为大写
            groupFile.setFileSize(file.getSize());
            groupFile.setFileUrl("/api/group-files/" + groupId + "/" + uniqueFileName);
            groupFile.setDescription(description);
            groupFile.setDownloadCount(0);
            groupFile.setStatus("AVAILABLE");
            groupFile.setUploadedAt(new Date());
            groupFile.setUpdatedAt(new Date());
            
            save(groupFile);
            
            return groupFile.getFileId();
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public boolean updateFileInfo(GroupFile groupFile) {
        // 检查是否是上传者
        GroupFile original = getById(groupFile.getFileId());
        if (original == null || !original.getUploaderId().equals(groupFile.getUploaderId())) {
            return false; // 非上传者无权更新
        }
        
        groupFile.setUpdatedAt(new Date());
        return updateById(groupFile);
    }

    @Override
    @Transactional
    public boolean deleteFile(Long fileId, Long userId) {
        // 获取文件信息
        GroupFile file = getById(fileId);
        if (file == null) {
            return false;
        }
        
        // 检查是否是上传者或管理员
        boolean isUploader = file.getUploaderId().equals(userId);
        
        if (!isUploader) {
            // 检查是否是小组管理员或创建者
            LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(GroupMember::getGroupId, file.getGroupId())
                    .eq(GroupMember::getUserId, userId)
                    .in(GroupMember::getRole, "ADMIN", "CREATOR");
            
            if (groupMemberMapper.selectCount(queryWrapper) == 0) {
                return false; // 既不是上传者也不是管理员，无权删除
            }
        }
        
        // 逻辑删除文件
        file.setStatus("DELETED");
        file.setUpdatedAt(new Date());
        return updateById(file);
    }

    @Override
    public GroupFile getFileDetail(Long fileId) {
        return getById(fileId);
    }
    
    @Override
    public GroupFile getByFileUrl(String fileUrl) {
        LambdaQueryWrapper<GroupFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupFile::getFileUrl, fileUrl)
                   .eq(GroupFile::getStatus, "AVAILABLE");
        return getOne(queryWrapper);
    }

    @Override
    public boolean increaseDownloadCount(Long fileId) {
        GroupFile file = getById(fileId);
        if (file == null) {
            return false;
        }
        
        file.setDownloadCount(file.getDownloadCount() + 1);
        return updateById(file);
    }
    
    @Override
    public Path getPhysicalFilePath(Long groupId, String fileName) {
        return Paths.get(uploadPath, "group-files", groupId.toString(), fileName);
    }
}
