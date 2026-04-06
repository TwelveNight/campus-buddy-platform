package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.entity.GroupFile;
import com.example.campusbuddy.entity.GroupMember;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.mapper.GroupFileMapper;
import com.example.campusbuddy.mapper.GroupMemberMapper;
import com.example.campusbuddy.mapper.UserMapper;
import com.example.campusbuddy.service.GroupFileService;
import com.example.campusbuddy.service.UploadService;
import com.example.campusbuddy.vo.GroupFileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 学习小组文件服务实现类
 */
@Service
public class GroupFileServiceImpl extends ServiceImpl<GroupFileMapper, GroupFile> implements GroupFileService {

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private UserMapper userMapper;

    @Override
    public IPage<GroupFileVO> queryGroupFiles(Long groupId, Integer pageNum, Integer pageSize, String fileType) {
        LambdaQueryWrapper<GroupFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupFile::getGroupId, groupId)
                .eq(GroupFile::getStatus, "AVAILABLE");

        if (StringUtils.hasText(fileType)) {
            queryWrapper.eq(GroupFile::getFileType, fileType);
        }

        queryWrapper.orderByDesc(GroupFile::getUploadedAt);

        IPage<GroupFile> filePage = page(new Page<>(pageNum, pageSize), queryWrapper);

        // 批量查询上传者信息
        List<GroupFile> records = filePage.getRecords();
        Set<Long> uploaderIds = records.stream()
                .map(GroupFile::getUploaderId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = java.util.Collections.emptyMap();
        if (!uploaderIds.isEmpty()) {
            LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
            userQuery.in(User::getUserId, uploaderIds);
            userMap = userMapper.selectList(userQuery).stream()
                    .collect(Collectors.toMap(User::getUserId, u -> u));
        }

        // 转换为 VO
        Map<Long, User> finalUserMap = userMap;
        List<GroupFileVO> voList = records.stream().map(f -> {
            GroupFileVO vo = new GroupFileVO();
            vo.setFileId(f.getFileId());
            vo.setGroupId(f.getGroupId());
            vo.setUploaderId(f.getUploaderId());
            vo.setFileName(f.getFileName());
            vo.setFileType(f.getFileType());
            vo.setFileSize(f.getFileSize());
            vo.setFileUrl(f.getFileUrl());
            vo.setDescription(f.getDescription());
            vo.setDownloadCount(f.getDownloadCount());
            vo.setStatus(f.getStatus());
            vo.setCreatedAt(f.getUploadedAt());
            vo.setUpdatedAt(f.getUpdatedAt());
            User uploader = f.getUploaderId() != null ? finalUserMap.get(f.getUploaderId()) : null;
            if (uploader != null) {
                vo.setUploaderName(uploader.getNickname() != null ? uploader.getNickname() : uploader.getUsername());
                vo.setUploaderAvatar(uploader.getAvatarUrl());
            }
            return vo;
        }).collect(Collectors.toList());

        Page<GroupFileVO> voPage = new Page<>(filePage.getCurrent(), filePage.getSize(), filePage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional
    public Long uploadFile(Long groupId, Long uploaderId, MultipartFile file, String description) {
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "unnamed-file";
            }

            String fileExtension = "";
            if (originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 使用UploadService进行文件上传
            String fileUrl = uploadService.uploadGroupFile(groupId, uploaderId, file, "group-files");

            // 保存文件信息
            GroupFile groupFile = new GroupFile();
            groupFile.setGroupId(groupId);
            groupFile.setUploaderId(uploaderId);
            groupFile.setFileName(originalFilename);
            groupFile.setFileType(fileExtension.isEmpty() ? "UNKNOWN" : fileExtension.substring(1).toUpperCase());
            groupFile.setFileSize(file.getSize());
            groupFile.setFileUrl(fileUrl);
            groupFile.setDescription(description);
            groupFile.setDownloadCount(0);
            groupFile.setStatus("AVAILABLE");
            groupFile.setUploadedAt(new Date());
            groupFile.setUpdatedAt(new Date());
            save(groupFile);
            return groupFile.getFileId();
        } catch (Exception e) {
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
        GroupFile file = getById(fileId);
        if (file == null) {
            return false;
        }
        boolean isUploader = file.getUploaderId().equals(userId);
        if (!isUploader) {
            LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(GroupMember::getGroupId, file.getGroupId())
                    .eq(GroupMember::getUserId, userId)
                    .in(GroupMember::getRole, "ADMIN", "CREATOR");
            if (groupMemberMapper.selectCount(queryWrapper) == 0) {
                return false;
            }
        }
        // 逻辑删除
        file.setStatus("DELETED");
        file.setUpdatedAt(new Date());
        boolean dbResult = updateById(file);

        // 使用UploadService删除七牛云中的文件
        uploadService.deleteFromQiniu(file.getFileUrl());

        return dbResult;
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
        // 七牛云不需要物理路径，返回null或抛异常
        throw new UnsupportedOperationException("七牛云存储不支持本地物理路径");
    }
}
