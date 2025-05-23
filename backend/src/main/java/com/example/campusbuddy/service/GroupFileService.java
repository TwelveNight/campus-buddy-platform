package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campusbuddy.entity.GroupFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

/**
 * 学习小组文件服务接口
 */
public interface GroupFileService extends IService<GroupFile> {
    /**
     * 分页查询小组文件
     * @param groupId 小组ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param fileType 文件类型（可选）
     * @return 文件分页列表
     */
    IPage<GroupFile> queryGroupFiles(Long groupId, Integer pageNum, Integer pageSize, String fileType);
    
    /**
     * 上传文件
     * @param groupId 小组ID
     * @param uploaderId 上传者ID
     * @param file 上传的文件
     * @param description 文件描述
     * @return 文件ID
     */
    Long uploadFile(Long groupId, Long uploaderId, MultipartFile file, String description);
    
    /**
     * 更新文件信息
     * @param groupFile 文件信息
     * @return 是否更新成功
     */
    boolean updateFileInfo(GroupFile groupFile);
    
    /**
     * 删除文件
     * @param fileId 文件ID
     * @param userId 执行删除操作的用户ID
     * @return 是否删除成功
     */
    boolean deleteFile(Long fileId, Long userId);
    
    /**
     * 获取文件详情
     * @param fileId 文件ID
     * @return 文件详情
     */
    GroupFile getFileDetail(Long fileId);
    
    /**
     * 根据文件URL获取文件
     * @param fileUrl 文件URL
     * @return 文件详情
     */
    GroupFile getByFileUrl(String fileUrl);
    
    /**
     * 增加下载计数
     * @param fileId 文件ID
     * @return 是否更新成功
     */
    boolean increaseDownloadCount(Long fileId);
    
    /**
     * 获取文件的物理路径
     * @param groupId 小组ID
     * @param fileName 文件名
     * @return 文件的物理路径
     */
    Path getPhysicalFilePath(Long groupId, String fileName);
}
