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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Date;
import java.util.UUID;

/**
 * 学习小组文件服务实现类
 */
@Service
public class GroupFileServiceImpl extends ServiceImpl<GroupFileMapper, GroupFile> implements GroupFileService {

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    @Value("${qiniu.access-key}")
    private String qiniuAccessKey;
    @Value("${qiniu.secret-key}")
    private String qiniuSecretKey;
    @Value("${qiniu.bucket}")
    private String qiniuBucket;
    @Value("${qiniu.domain}")
    private String qiniuDomain;

    private UploadManager getUploadManager() {
        Configuration cfg = new Configuration(Region.autoRegion());
        return new UploadManager(cfg);
    }

    private Auth getAuth() {
        return Auth.create(qiniuAccessKey, qiniuSecretKey);
    }

    private BucketManager getBucketManager() {
        Configuration cfg = new Configuration(Region.autoRegion());
        return new BucketManager(getAuth(), cfg);
    }

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
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "unnamed-file";
            }
            String fileExtension = "";
            if (originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = "group-" + groupId + "/" + UUID.randomUUID() + fileExtension;

            // 上传到七牛云
            UploadManager uploadManager = getUploadManager();
            Auth auth = getAuth();
            String upToken = auth.uploadToken(qiniuBucket);
            Response response = uploadManager.put(file.getBytes(), uniqueFileName, upToken);
            if (!response.isOK()) {
                throw new RuntimeException("七牛云上传失败: " + response.bodyString());
            }
            // 解析返回
            ObjectMapper mapper = new ObjectMapper();
            DefaultPutRet putRet = mapper.readValue(response.bodyString(), DefaultPutRet.class);
            String fileUrl = qiniuDomain + "/" + putRet.key;

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
        // 七牛云物理删除
        try {
            String key = file.getFileUrl().replace(qiniuDomain + "/", "");
            getBucketManager().delete(qiniuBucket, key);
        } catch (QiniuException e) {
            // 记录错误但不影响逻辑删除
            e.printStackTrace();
        }
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
