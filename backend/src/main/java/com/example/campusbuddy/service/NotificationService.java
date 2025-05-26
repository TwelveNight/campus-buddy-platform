package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campusbuddy.dto.NotificationCreateDTO;
import com.example.campusbuddy.entity.Notification;
import com.example.campusbuddy.vo.NotificationVO;

import java.util.List;

/**
 * 消息通知Service接口
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 创建系统通知
     *
     * @param dto 通知创建DTO
     * @return 通知ID
     */
    Long createSystemNotification(NotificationCreateDTO dto);

    /**
     * 创建用户通知
     *
     * @param dto 通知创建DTO
     * @return 通知ID
     */
    Long createUserNotification(NotificationCreateDTO dto);

    /**
     * 分页获取用户通知列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @param type   通知类型
     * @param isRead 是否已读 (新增)
     * @return 分页通知列表
     */
    IPage<NotificationVO> getUserNotifications(Long userId, int page, int size, String type, Boolean isRead);

    /**
     * 获取用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    int countUnreadNotifications(Long userId);

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @param userId         用户ID
     * @return 是否成功
     */
    boolean markAsRead(Long notificationId, Long userId);

    /**
     * 标记用户所有通知为已读
     *
     * @param userId 用户ID
     * @return 更新数量
     */
    int markAllAsRead(Long userId);

    /**
     * 删除通知
     *
     * @param notificationId 通知ID
     * @param userId         用户ID
     * @return 是否成功
     */
    boolean deleteNotification(Long notificationId, Long userId);

    /**
     * 创建互助申请通知
     *
     * @param helpInfoId    互助任务ID
     * @param publisherId   发布者ID
     * @param applicantId   申请者ID
     * @param applicantName 申请者名称
     * @return 通知ID
     */
    Long createHelpApplicationNotification(Long helpInfoId, Long publisherId, Long applicantId, String applicantName);

    /**
     * 创建申请结果通知
     *
     * @param helpInfoId  互助任务ID
     * @param applicantId 申请者ID
     * @param isAccepted  是否接受
     * @param helpTitle   互助标题
     * @param operatorId  操作者ID（可选，为null时作为系统通知）
     * @param operatorName 操作者名称（可选）
     * @return 通知ID
     */
    Long createApplicationResultNotification(Long helpInfoId, Long applicantId, boolean isAccepted, String helpTitle, Long operatorId, String operatorName);

    /**
     * 创建互助完成通知
     *
     * @param helpInfoId  互助任务ID
     * @param recipientId 接收者ID
     * @param helpTitle   互助标题
     * @return 通知ID
     */
    Long createHelpCompletedNotification(Long helpInfoId, Long recipientId, String helpTitle);

    /**
     * 创建评价通知
     *
     * @param reviewId    评价ID
     * @param reviewedId  被评价者ID
     * @param reviewerId  评价者ID
     * @param reviewerName 评价者名称
     * @param relatedId   相关业务ID
     * @return 通知ID
     */
    Long createReviewNotification(Long reviewId, Long reviewedId, Long reviewerId, String reviewerName, Long relatedId);

    /**
     * 创建小组加入申请结果通知
     *
     * @param groupId    小组ID
     * @param userId     用户ID
     * @param isApproved 是否批准
     * @param groupName  小组名称
     * @return 通知ID
     */
    Long createGroupJoinResultNotification(Long groupId, Long userId, boolean isApproved, String groupName);

    /**
     * 创建小组加入申请通知
     *
     * @param groupId       小组ID
     * @param applicantId   申请者ID
     * @param applicantName 申请者名称
     * @param groupName     小组名称
     * @param adminIds      管理员ID列表
     * @return 通知ID列表
     */
    List<Long> createGroupJoinApplicationNotification(Long groupId, Long applicantId, String applicantName, String groupName, List<Long> adminIds);

    /**
     * 创建小组邀请通知
     *
     * @param groupId       小组ID
     * @param inviteeId     被邀请者ID
     * @param inviterId     邀请者ID
     * @param inviterName   邀请者名称
     * @param groupName     小组名称
     * @return 通知ID
     */
    Long createGroupInvitationNotification(Long groupId, Long inviteeId, Long inviterId, String inviterName, String groupName);

    /**
     * 创建小组公告通知
     *
     * @param groupId     小组ID
     * @param memberIds   成员ID列表
     * @param title       公告标题
     * @param content     公告内容
     * @param publisherId 发布者ID
     * @return 通知ID列表
     */
    List<Long> createGroupAnnouncementNotification(Long groupId, List<Long> memberIds, String title, String content, Long publisherId);

    /**
     * 创建设为管理员通知
     *
     * @param groupId     小组ID
     * @param userId      用户ID
     * @param operatorId  操作者ID
     * @param operatorName 操作者名称
     * @param groupName   小组名称
     * @return 通知ID
     */
    Long createGroupAdminAssignedNotification(Long groupId, Long userId, Long operatorId, String operatorName, String groupName);
}
