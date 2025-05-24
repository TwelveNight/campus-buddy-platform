package com.example.campusbuddy.service.impl;

import com.example.campusbuddy.config.QiniuConfig;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.service.UploadService;
import com.example.campusbuddy.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务实现类
 */
@Service
public class UploadServiceImpl implements UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadServiceImpl.class);

    // 允许的图片类型
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"
    );

    // 最大文件大小 (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Autowired
    private QiniuConfig.QiniuProperties qiniuProperties;

    @Autowired
    private UserService userService;

    /**
     * 获取七牛云上传管理器
     */
    @Override
    public UploadManager getUploadManager() {
        Configuration cfg = new Configuration(Region.autoRegion());
        return new UploadManager(cfg);
    }

    /**
     * 获取七牛云认证对象
     */
    @Override
    public Auth getAuth() {
        return Auth.create(qiniuProperties.getAccessKey(), qiniuProperties.getSecretKey());
    }

    /**
     * 获取七牛云存储管理器
     */
    @Override
    public BucketManager getBucketManager() {
        Configuration cfg = new Configuration(Region.autoRegion());
        return new BucketManager(getAuth(), cfg);
    }

    @Override
    @Transactional
    public String uploadAvatar(Long userId, MultipartFile file) {
        try {
            // 验证文件类型和大小
            validateImageFile(file);

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "avatar.jpg";
            }

            String fileExtension = "";
            if (originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            } else {
                fileExtension = ".jpg"; // 默认扩展名
            }

            // 生成唯一文件名，使用用户ID作为前缀
            String uniqueFileName = "avatar/" + userId + "-" + UUID.randomUUID() + fileExtension;

            // 上传到七牛云
            String fileUrl = uploadToQiniu(file.getBytes(), uniqueFileName);

            // 更新用户头像URL
            User user = userService.getById(userId);
            if (user != null) {
                user.setAvatarUrl(fileUrl);
                userService.updateById(user);
            }

            return fileUrl;
        } catch (Exception e) {
            log.error("头像上传失败", e);
            throw new RuntimeException("头像上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadImage(Long userId, MultipartFile file) {
        try {
            // 验证文件类型和大小
            validateImageFile(file);

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "image.jpg";
            }

            String fileExtension = "";
            if (originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            } else {
                fileExtension = ".jpg"; // 默认扩展名
            }

            // 生成唯一文件名，使用用户ID作为前缀
            String uniqueFileName = "images/" + userId + "-" + UUID.randomUUID() + fileExtension;

            // 上传到七牛云
            return uploadToQiniu(file.getBytes(), uniqueFileName);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            throw new RuntimeException("图片上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadGroupFile(Long groupId, Long userId, MultipartFile file, String folderPrefix) {
        try {
            // 检查文件大小
            if (file.getSize() <= 0) {
                throw new IllegalArgumentException("文件不能为空");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "file";
            }

            String fileExtension = "";
            if (originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 生成唯一文件名，使用学习小组ID和用户ID作为前缀
            String uniqueFileName = folderPrefix + "/group-" + groupId + "/user-" + userId + "-" + UUID.randomUUID() + fileExtension;

            // 上传到七牛云
            return uploadToQiniu(file.getBytes(), uniqueFileName);
        } catch (Exception e) {
            log.error("小组文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 上传文件到七牛云
     */
    @Override
    public String uploadToQiniu(byte[] fileData, String fileName) throws Exception {
        UploadManager uploadManager = getUploadManager();
        Auth auth = getAuth();
        String upToken = auth.uploadToken(qiniuProperties.getBucket());

        Response response = uploadManager.put(fileData, fileName, upToken);
        if (!response.isOK()) {
            throw new RuntimeException("七牛云上传失败: " + response.bodyString());
        }

        // 解析返回结果
        ObjectMapper mapper = new ObjectMapper();
        DefaultPutRet putRet = mapper.readValue(response.bodyString(), DefaultPutRet.class);

        // 返回完整的URL
        return qiniuProperties.getDomain() + "/" + putRet.key;
    }

    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过5MB");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("只支持JPG、PNG、GIF、BMP和WebP格式的图片");
        }
    }

    /**
     * 从七牛云删除文件
     */
    @Override
    public boolean deleteFromQiniu(String fileUrl) {
        try {
            if (fileUrl == null || !fileUrl.startsWith(qiniuProperties.getDomain())) {
                return false;
            }

            String key = fileUrl.replace(qiniuProperties.getDomain() + "/", "");
            getBucketManager().delete(qiniuProperties.getBucket(), key);
            return true;
        } catch (Exception e) {
            log.error("七牛云删除文件失败", e);
            return false;
        }
    }
}
