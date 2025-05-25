package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.dto.PrivateMessageDTO;
import com.example.campusbuddy.entity.PrivateMessage;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.mapper.PrivateMessageMapper;
import com.example.campusbuddy.service.PrivateMessageService;
import com.example.campusbuddy.service.UserService;
import com.example.campusbuddy.vo.PrivateMessageVO;
import com.example.campusbuddy.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 私信Service实现类
 */
@Service
public class PrivateMessageServiceImpl extends ServiceImpl<PrivateMessageMapper, PrivateMessage> implements PrivateMessageService {

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public Long sendMessage(Long senderId, PrivateMessageDTO dto) {
        PrivateMessage message = new PrivateMessage();
        message.setSenderId(senderId);
        message.setRecipientId(dto.getRecipientId());
        message.setContent(dto.getContent());
        message.setIsRead(false);
        
        save(message);
        
        // 通过WebSocket发送实时消息
        User sender = userService.getById(senderId);
        if (sender != null) {
            WebSocketServer.sendPrivateMessage(
                dto.getRecipientId(), 
                senderId, 
                sender.getNickname(), 
                dto.getContent(), 
                message.getMessageId()
            );
        }
        
        return message.getMessageId();
    }

    @Override
    public IPage<PrivateMessageVO> getChatHistory(Long userId, Long otherUserId, int page, int size) {
        Page<PrivateMessage> pageParam = new Page<>(page, size);
        
        // 查询两个用户之间的所有消息
        LambdaQueryWrapper<PrivateMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(PrivateMessage::getSenderId, userId).eq(PrivateMessage::getRecipientId, otherUserId)
                .or()
                .eq(PrivateMessage::getSenderId, otherUserId).eq(PrivateMessage::getRecipientId, userId));
        
        wrapper.orderByAsc(PrivateMessage::getCreatedAt);
        
        IPage<PrivateMessage> messagePage = page(pageParam, wrapper);
        
        // 自动标记接收到的消息为已读
        List<PrivateMessage> messageList = messagePage.getRecords();
        List<Long> unreadMessageIds = messageList.stream()
                .filter(msg -> msg.getRecipientId().equals(userId) && !msg.getIsRead())
                .map(PrivateMessage::getMessageId)
                .collect(Collectors.toList());
        
        if (!unreadMessageIds.isEmpty()) {
            for (Long messageId : unreadMessageIds) {
                markAsRead(messageId, userId);
            }
        }
        
        // 获取用户信息
        User currentUser = userService.getById(userId);
        User otherUser = userService.getById(otherUserId);
        
        // 转换为VO
        IPage<PrivateMessageVO> voPage = messagePage.convert(message -> {
            PrivateMessageVO vo = new PrivateMessageVO();
            BeanUtils.copyProperties(message, vo);
            
            // 设置发送者和接收者信息
            if (message.getSenderId().equals(userId)) {
                vo.setSenderName(currentUser.getNickname());
                vo.setSenderAvatar(currentUser.getAvatarUrl());
                vo.setRecipientName(otherUser.getNickname());
                vo.setRecipientAvatar(otherUser.getAvatarUrl());
            } else {
                vo.setSenderName(otherUser.getNickname());
                vo.setSenderAvatar(otherUser.getAvatarUrl());
                vo.setRecipientName(currentUser.getNickname());
                vo.setRecipientAvatar(currentUser.getAvatarUrl());
            }
            
            return vo;
        });
        
        return voPage;
    }

    @Override
    public IPage<Map<String, Object>> getChatSessionList(Long userId, int page, int size) {
        Page<Map<String, Object>> pageParam = new Page<>(page, size);
        
        // 查询与用户相关的所有聊天
        LambdaQueryWrapper<PrivateMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateMessage::getSenderId, userId).or().eq(PrivateMessage::getRecipientId, userId);
        wrapper.orderByDesc(PrivateMessage::getCreatedAt);
        
        List<PrivateMessage> allMessages = list(wrapper);
        
        // 按照聊天对象分组，获取每个对话的最新消息
        Map<Long, PrivateMessage> latestMessageMap = new HashMap<>();
        
        for (PrivateMessage message : allMessages) {
            Long chatPartnerId;
            
            if (message.getSenderId().equals(userId)) {
                chatPartnerId = message.getRecipientId();
            } else {
                chatPartnerId = message.getSenderId();
            }
            
            if (!latestMessageMap.containsKey(chatPartnerId) || 
                    message.getCreatedAt().isAfter(latestMessageMap.get(chatPartnerId).getCreatedAt())) {
                latestMessageMap.put(chatPartnerId, message);
            }
        }
        
        // 构建会话列表
        List<Map<String, Object>> sessionList = new ArrayList<>();
        
        for (Map.Entry<Long, PrivateMessage> entry : latestMessageMap.entrySet()) {
            Long partnerId = entry.getKey();
            PrivateMessage latestMessage = entry.getValue();
            
            User partner = userService.getById(partnerId);
            if (partner == null) continue;
            
            Map<String, Object> session = new HashMap<>();
            session.put("userId", partnerId);
            session.put("nickname", partner.getNickname());
            session.put("avatarUrl", partner.getAvatarUrl());
            session.put("lastMessage", latestMessage.getContent());
            session.put("lastMessageTime", latestMessage.getCreatedAt());
            
            // 计算未读消息数
            LambdaQueryWrapper<PrivateMessage> unreadWrapper = new LambdaQueryWrapper<>();
            unreadWrapper.eq(PrivateMessage::getSenderId, partnerId)
                    .eq(PrivateMessage::getRecipientId, userId)
                    .eq(PrivateMessage::getIsRead, false);
            long unreadCount = count(unreadWrapper);
            
            session.put("unreadCount", unreadCount);
            
            sessionList.add(session);
        }
        
        // 按最后消息时间排序
        sessionList.sort((s1, s2) -> ((LocalDateTime) s2.get("lastMessageTime"))
                .compareTo((LocalDateTime) s1.get("lastMessageTime")));
        
        // 手动分页
        int total = sessionList.size();
        int fromIndex = (int) ((page - 1) * size);
        int toIndex = Math.min(fromIndex + size, total);
        
        if (fromIndex >= total) {
            sessionList = new ArrayList<>();
        } else {
            sessionList = sessionList.subList(fromIndex, toIndex);
        }
        
        // 构建分页结果
        IPage<Map<String, Object>> result = new Page<>(page, size, total);
        result.setRecords(sessionList);
        
        return result;
    }

    @Override
    public int countUnreadMessages(Long userId) {
        return baseMapper.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId, Long otherUserId) {
        return baseMapper.markAllAsReadBySender(userId, otherUserId);
    }

    @Override
    @Transactional
    public boolean markAsRead(Long messageId, Long userId) {
        // 确保消息属于该用户
        LambdaQueryWrapper<PrivateMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateMessage::getMessageId, messageId)
                .eq(PrivateMessage::getRecipientId, userId);
        
        PrivateMessage message = getOne(wrapper);
        
        if (message == null) {
            return false;
        }
        
        // 标记为已读
        message.setIsRead(true);
        return updateById(message);
    }

    @Override
    @Transactional
    public boolean deleteMessage(Long messageId, Long userId) {
        // 确保消息与该用户相关
        LambdaQueryWrapper<PrivateMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateMessage::getMessageId, messageId)
                .and(w -> w.eq(PrivateMessage::getSenderId, userId).or().eq(PrivateMessage::getRecipientId, userId));
        
        return remove(wrapper);
    }
}
