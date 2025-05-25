package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campusbuddy.dto.PrivateMessageDTO;
import com.example.campusbuddy.entity.PrivateMessage;
import com.example.campusbuddy.vo.PrivateMessageVO;

import java.util.List;
import java.util.Map;

/**
 * 私信Service接口
 */
public interface PrivateMessageService extends IService<PrivateMessage> {

    /**
     * 发送私信
     *
     * @param senderId 发送者ID
     * @param dto      私信DTO
     * @return 私信ID
     */
    Long sendMessage(Long senderId, PrivateMessageDTO dto);

    /**
     * 获取与指定用户的聊天记录
     *
     * @param userId   当前用户ID
     * @param otherUserId 对方用户ID
     * @param page     页码
     * @param size     每页大小
     * @return 分页聊天记录
     */
    IPage<PrivateMessageVO> getChatHistory(Long userId, Long otherUserId, int page, int size);

    /**
     * 获取用户的聊天会话列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 聊天会话列表
     */
    IPage<Map<String, Object>> getChatSessionList(Long userId, int page, int size);

    /**
     * 获取未读私信数量
     *
     * @param userId 用户ID
     * @return 未读私信数量
     */
    int countUnreadMessages(Long userId);

    /**
     * 标记与指定用户的所有私信为已读
     *
     * @param userId     当前用户ID
     * @param otherUserId 对方用户ID
     * @return 更新数量
     */
    int markAllAsRead(Long userId, Long otherUserId);

    /**
     * 标记指定私信为已读
     *
     * @param messageId 私信ID
     * @param userId    用户ID
     * @return 是否成功
     */
    boolean markAsRead(Long messageId, Long userId);

    /**
     * 删除私信
     *
     * @param messageId 私信ID
     * @param userId    用户ID
     * @return 是否成功
     */
    boolean deleteMessage(Long messageId, Long userId);
}
