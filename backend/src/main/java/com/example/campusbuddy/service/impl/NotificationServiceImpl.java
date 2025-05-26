package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.dto.NotificationCreateDTO;
import com.example.campusbuddy.entity.Notification;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.mapper.NotificationMapper;
import com.example.campusbuddy.service.NotificationService;
import com.example.campusbuddy.service.UserService;
import com.example.campusbuddy.vo.NotificationVO;
import com.example.campusbuddy.websocket.UserWebSocketHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 消息通知Service实现类
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public Long createSystemNotification(NotificationCreateDTO dto) {
        // 首先获取所有活跃用户
        List<User> activeUsers = userService.list(
            new LambdaQueryWrapper<User>()
                .eq(User::getStatus, "ACTIVE")
        );
        
        Long firstNotificationId = null;
        
        // 为每个用户创建一条系统通知
        for (User user : activeUsers) {
            Notification notification = new Notification();
            BeanUtils.copyProperties(dto, notification);
            notification.setType(dto.getType());
            notification.setIsRead(false);
            notification.setRecipientId(user.getUserId()); // 设置接收者ID为当前用户
            save(notification);
            
            // 保存第一个通知ID作为返回值
            if (firstNotificationId == null) {
                firstNotificationId = notification.getNotificationId();
            }
            
            // 通过WebSocket发送实时通知
            String relatedLink = generateRelatedLink(dto.getType(), dto.getRelatedId());
            UserWebSocketHandler.sendNotification(
                user.getUserId(), 
                dto.getTitle(), 
                dto.getContent(), 
                dto.getType(), 
                relatedLink
            );
        }
        
        return firstNotificationId;
    }

    @Override
    @Transactional
    public Long createUserNotification(NotificationCreateDTO dto) {
        Notification notification = new Notification();
        BeanUtils.copyProperties(dto, notification);
        notification.setType(dto.getType());
        notification.setIsRead(false);
        save(notification);
        
        // 通过WebSocket发送实时通知
        String relatedLink = generateRelatedLink(dto.getType(), dto.getRelatedId());
        UserWebSocketHandler.sendNotification(
            dto.getRecipientId(), 
            dto.getTitle(), 
            dto.getContent(), 
            dto.getType(), 
            relatedLink
        );
        
        return notification.getNotificationId();
    }

    @Override
    public IPage<NotificationVO> getUserNotifications(Long userId, int page, int size, String type, Boolean isRead) {
        Page<Notification> pageRequest = new Page<>(page, size);
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getRecipientId, userId);

        if (StringUtils.hasText(type) && !"all".equalsIgnoreCase(type)) { // 修改这里
            // 支持多种类型查询，逗号分隔
            List<String> typeList = List.of(type.split(","));
            queryWrapper.in(Notification::getType, typeList);
        }

        if (isRead != null) { // 添加已读状态过滤
            queryWrapper.eq(Notification::getIsRead, isRead);
        }

        queryWrapper.orderByDesc(Notification::getCreatedAt);

        IPage<Notification> notificationPage = this.page(pageRequest, queryWrapper);
        IPage<NotificationVO> voPage = notificationPage.convert(notification -> {
            NotificationVO vo = new NotificationVO();
            BeanUtils.copyProperties(notification, vo);
            if (notification.getSenderId() != null) {
                User sender = userService.getById(notification.getSenderId());
                if (sender != null) {
                    vo.setSenderName(sender.getNickname());
                    vo.setSenderAvatar(sender.getAvatarUrl());
                }
            }
            // 传递senderId用于FRIEND_REMOVED跳转
            vo.setRelatedLink(generateRelatedLink(notification.getType(), notification.getRelatedId(), notification.getSenderId()));
            return vo;
        });
        return voPage;
    }

    @Override
    public int countUnreadNotifications(Long userId) {
        return baseMapper.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public boolean markAsRead(Long notificationId, Long userId) {
        // 确保通知属于该用户
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getNotificationId, notificationId)
                .eq(Notification::getRecipientId, userId);
        
        Notification notification = getOne(wrapper);
        
        if (notification == null) {
            return false;
        }
        
        // 标记为已读
        notification.setIsRead(true);
        return updateById(notification);
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        return baseMapper.markAllAsRead(userId);
    }

    @Override
    @Transactional
    public boolean deleteNotification(Long notificationId, Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getNotificationId, notificationId)
                .eq(Notification::getRecipientId, userId);
        
        return remove(wrapper);
    }

    @Override
    @Transactional
    public Long createHelpApplicationNotification(Long helpInfoId, Long publisherId, Long applicantId, String applicantName) {
        NotificationCreateDTO dto = new NotificationCreateDTO();
        dto.setRecipientId(publisherId);
        dto.setSenderId(applicantId);
        dto.setType(Notification.NotificationType.HELP_NEW_APPLICATION.name());
        dto.setTitle("新的互助申请");
        dto.setContent(applicantName + " 申请了您发布的互助任务");
        dto.setRelatedId(helpInfoId);
        
        return createUserNotification(dto);
    }

    @Override
    @Transactional
    public Long createApplicationResultNotification(Long helpInfoId, Long applicantId, boolean isAccepted, String helpTitle, Long operatorId, String operatorName) {
        NotificationCreateDTO dto = new NotificationCreateDTO();
        dto.setRecipientId(applicantId);
        dto.setSenderId(operatorId); // 设置操作者ID，如果为null则为系统通知
        dto.setType(isAccepted ? 
                Notification.NotificationType.HELP_APPLICATION_ACCEPTED.name() : 
                Notification.NotificationType.HELP_APPLICATION_REJECTED.name());
        dto.setTitle(isAccepted ? "互助申请已接受" : "互助申请被拒绝");
        
        // 根据是否有操作者信息调整通知内容
        String content;
        if (operatorId != null && operatorName != null) {
            content = operatorName + " " + (isAccepted ? "接受了" : "拒绝了") + " 您申请的互助任务 \"" + helpTitle + "\"";
        } else {
            content = "您申请的互助任务 \"" + helpTitle + "\" " + (isAccepted ? "已被接受" : "被拒绝了");
        }
        dto.setContent(content);
        dto.setRelatedId(helpInfoId);
        
        return createUserNotification(dto);
    }

    @Override
    @Transactional
    public Long createHelpCompletedNotification(Long helpInfoId, Long recipientId, String helpTitle) {
        NotificationCreateDTO dto = new NotificationCreateDTO();
        dto.setRecipientId(recipientId);
        dto.setType(Notification.NotificationType.HELP_COMPLETED.name());
        dto.setTitle("互助已完成");
        dto.setContent("互助任务 \"" + helpTitle + "\" 已标记为完成");
        dto.setRelatedId(helpInfoId);
        
        return createUserNotification(dto);
    }

    @Override
    @Transactional
    public Long createReviewNotification(Long reviewId, Long reviewedId, Long reviewerId, String reviewerName, Long relatedId) {
        NotificationCreateDTO dto = new NotificationCreateDTO();
        dto.setRecipientId(reviewedId);
        dto.setSenderId(reviewerId);
        dto.setType(Notification.NotificationType.HELP_NEW_REVIEW.name());
        dto.setTitle("收到新的评价");
        dto.setContent(reviewerName + " 给您提交了一条新的评价");
        dto.setRelatedId(relatedId);
        
        return createUserNotification(dto);
    }

    @Override
    @Transactional
    public Long createGroupJoinResultNotification(Long groupId, Long userId, boolean isApproved, String groupName) {
        NotificationCreateDTO dto = new NotificationCreateDTO();
        dto.setRecipientId(userId);
        dto.setType(isApproved ? 
                Notification.NotificationType.GROUP_JOIN_APPROVED.name() : 
                Notification.NotificationType.GROUP_JOIN_REJECTED.name());
        dto.setTitle(isApproved ? "小组申请已批准" : "小组申请被拒绝");
        dto.setContent("您申请加入小组 \"" + groupName + "\" 的请求" + (isApproved ? "已被批准" : "被拒绝了"));
        dto.setRelatedId(groupId);
        
        return createUserNotification(dto);
    }

    @Override
    @Transactional
    public List<Long> createGroupJoinApplicationNotification(Long groupId, Long applicantId, String applicantName, String groupName, List<Long> adminIds) {
        List<Long> notificationIds = new ArrayList<>();
        
        for (Long adminId : adminIds) {
            NotificationCreateDTO dto = new NotificationCreateDTO();
            dto.setRecipientId(adminId);
            dto.setSenderId(applicantId);
            dto.setType(Notification.NotificationType.GROUP_JOIN_APPLICATION.name());
            dto.setTitle("新的小组加入申请");
            dto.setContent(applicantName + " 申请加入小组 \"" + groupName + "\"");
            dto.setRelatedId(groupId);
            
            Long notificationId = createUserNotification(dto);
            notificationIds.add(notificationId);
        }
        
        return notificationIds;
    }

    @Override
    @Transactional
    public Long createGroupInvitationNotification(Long groupId, Long inviteeId, Long inviterId, String inviterName, String groupName) {
        NotificationCreateDTO dto = new NotificationCreateDTO();
        dto.setRecipientId(inviteeId);
        dto.setSenderId(inviterId);
        dto.setType(Notification.NotificationType.GROUP_INVITATION.name());
        dto.setTitle("小组邀请");
        dto.setContent(inviterName + " 邀请您加入小组 \"" + groupName + "\"");
        dto.setRelatedId(groupId);
        
        return createUserNotification(dto);
    }

    @Override
    @Transactional
    public List<Long> createGroupAnnouncementNotification(Long groupId, List<Long> memberIds, String title, String content, Long publisherId) {
        List<Long> notificationIds = new ArrayList<>();
        
        for (Long memberId : memberIds) {
            if (!memberId.equals(publisherId)) {  // 不给发布者自己发通知
                NotificationCreateDTO dto = new NotificationCreateDTO();
                dto.setRecipientId(memberId);
                dto.setSenderId(publisherId);
                dto.setType(Notification.NotificationType.GROUP_ANNOUNCEMENT.name());
                dto.setTitle("小组公告: " + title);
                dto.setContent(content);
                dto.setRelatedId(groupId);
                
                Long notificationId = createUserNotification(dto);
                notificationIds.add(notificationId);
            }
        }
        
        return notificationIds;
    }

    @Override
    @Transactional
    public Long createGroupAdminAssignedNotification(Long groupId, Long userId, Long operatorId, String operatorName, String groupName) {
        NotificationCreateDTO dto = new NotificationCreateDTO();
        dto.setRecipientId(userId);
        dto.setSenderId(operatorId);
        dto.setType(Notification.NotificationType.GROUP_ADMIN_ASSIGNED.name());
        dto.setTitle("您已被设为小组管理员");
        dto.setContent(operatorName + " 将您设为了小组 \"" + groupName + "\" 的管理员");
        dto.setRelatedId(groupId);
        
        return createUserNotification(dto);
    }

    /**
     * 根据通知类型和相关ID生成前端路由链接
     */
    private String generateRelatedLink(String type, Long relatedId) {
        if (relatedId == null) {
            // 处理无需relatedId的类型
            return switch (type) {
                case "FRIEND_REQUEST_ACCEPTED", "FRIEND_REQUEST_REJECTED" -> "/friends?tab=friends";
                case "FRIEND_REQUEST" -> "/friends?tab=requests";
                default -> null; // 无法生成链接，可能需要其他处理
            };
        }

        return switch (type) {
            case "SYSTEM_ANNOUNCEMENT", "SYSTEM_ACTIVITY" -> "/announcement/" + relatedId;
            case "HELP_NEW_APPLICATION", "HELP_APPLICATION_ACCEPTED", "HELP_APPLICATION_REJECTED", "HELP_COMPLETED" ->
                    "/helpinfo/" + relatedId;
            case "HELP_NEW_REVIEW" ->
                // 评价通知跳转到用户的评价页面，显示收到的评价
                    "/reviews?type=received";
            case "GROUP_JOIN_APPLICATION" -> "/groups/" + relatedId + "/detail?tab=members&subtab=requests";
            case "GROUP_JOIN_APPROVED", "GROUP_JOIN_REJECTED", "GROUP_INVITATION", "GROUP_ANNOUNCEMENT",
                 "GROUP_ADMIN_ASSIGNED" -> "/groups/" + relatedId + "/detail";
            case "FRIEND_REQUEST_ACCEPTED" -> "/friends?tab=friends";
            case "FRIEND_REQUEST" -> "/friends?tab=requests";
            default -> null;
        };
    }

    /**
     * 根据通知类型和相关ID生成前端路由链接，支持senderId
     */
    private String generateRelatedLink(String type, Long relatedId, Long senderId) {

        if (relatedId == null) {
            // 处理无需relatedId的类型
            return switch (type) {
                case "FRIEND_REQUEST_ACCEPTED", "FRIEND_REQUEST_REJECTED" -> "/friends?tab=friends";
                case "FRIEND_REQUEST" -> "/friends?tab=requests";
                default -> null;
            };
        }
        return switch (type) {
            case "SYSTEM_ANNOUNCEMENT", "SYSTEM_ACTIVITY" -> "/announcement/" + relatedId;
            case "HELP_NEW_APPLICATION", "HELP_APPLICATION_ACCEPTED", "HELP_APPLICATION_REJECTED", "HELP_COMPLETED" ->
                    "/helpinfo/" + relatedId;
            case "HELP_NEW_REVIEW" -> "/reviews?type=received";
            case "GROUP_JOIN_APPLICATION" -> "/groups/" + relatedId + "/detail?tab=members&subtab=requests";
            case "GROUP_JOIN_APPROVED", "GROUP_JOIN_REJECTED", "GROUP_INVITATION", "GROUP_ANNOUNCEMENT",
                 "GROUP_ADMIN_ASSIGNED" -> "/groups/" + relatedId + "/detail";
            case "FRIEND_REQUEST_ACCEPTED" -> "/friends?tab=friends";
            case "FRIEND_REQUEST" -> "/friends?tab=requests";
            default -> null;
        };
    }
}
