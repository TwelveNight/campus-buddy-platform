package com.example.campusbuddy.service;

import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface UploadService {

    /**
     * 上传用户头像
     * @param userId 用户ID
     * @param file 头像文件
     * @return 头像URL
     */
    String uploadAvatar(Long userId, MultipartFile file);
    
    /**
     * 上传小组头像
     * @param file 头像文件
     * @return 头像URL
     */
    String uploadGroupAvatar(MultipartFile file);

    /**
     * 上传通用图片
     * @param userId 用户ID
     * @param file 图片文件
     * @return 图片URL
     */
    String uploadImage(Long userId, MultipartFile file);

    /**
     * 上传学习小组文件
     * @param groupId 小组ID
     * @param userId 上传者ID
     * @param file 文件
     * @param folderPrefix 文件夹前缀
     * @return 文件URL
     */
    String uploadGroupFile(Long groupId, Long userId, MultipartFile file, String folderPrefix);

    /**
     * 上传文件到七牛云
     * @param fileData 文件数据
     * @param fileName 文件名
     * @return 文件URL
     */
    String uploadToQiniu(byte[] fileData, String fileName) throws Exception;

    /**
     * 从七牛云删除文件
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    boolean deleteFromQiniu(String fileUrl);

    /**
     * 获取七牛云上传管理器
     * @return 上传管理器
     */
    UploadManager getUploadManager();

    /**
     * 获取七牛云认证对象
     * @return 认证对象
     */
    Auth getAuth();

    /**
     * 获取七牛云存储管理器
     * @return 存储管理器
     */
    BucketManager getBucketManager();
}
