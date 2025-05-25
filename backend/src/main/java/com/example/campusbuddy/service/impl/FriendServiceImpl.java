package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.dto.FriendRequestDTO;
import com.example.campusbuddy.entity.Friend;
import com.example.campusbuddy.entity.FriendRequest;
import com.example.campusbuddy.entity.Notification;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.exception.BusinessException;
import com.example.campusbuddy.mapper.FriendMapper;
import com.example.campusbuddy.service.FriendRequestService;
import com.example.campusbuddy.service.FriendService;
import com.example.campusbuddy.service.NotificationService;
import com.example.campusbuddy.service.UserService;
import com.example.campusbuddy.vo.FriendRequestVO;
import com.example.campusbuddy.vo.FriendVO;
import com.example.campusbuddy.vo.UserVO;
import com.example.campusbuddy.websocket.UserWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 好友服务实现类
 */
@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FriendRequestService friendRequestService;
    
    @Autowired
    private NotificationService notificationService;
    
    // WebSocketServer改为使用UserWebSocketHandler静态方法
    
    @Override
    @Transactional
    public Long applyFriend(Long requesterId, FriendRequestDTO dto) {
        // 参数校验
        if (dto.getRecipientId() == null) {
            throw new BusinessException("接收者ID不能为空");
        }
        
        Long recipientId = dto.getRecipientId();
        
        // 检查用户是否存在
        if (!userService.exists(recipientId)) {
            throw new BusinessException("接收者不存在");
        }
        
        // 不能添加自己为好友
        if (requesterId.equals(recipientId)) {
            throw new BusinessException("不能添加自己为好友");
        }
        
        // 检查是否已经是好友
        if (isFriend(requesterId, recipientId)) {
            throw new BusinessException("你们已经是好友了");
        }
        
        // 检查是否已经有待处理的申请
        LambdaQueryWrapper<FriendRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRequest::getRequesterId, requesterId)
                .eq(FriendRequest::getRecipientId, recipientId)
                .eq(FriendRequest::getStatus, "PENDING");
        
        if (friendRequestService.count(wrapper) > 0) {
            throw new BusinessException("已经发送过好友申请，请等待对方处理");
        }
        
        // 创建好友申请
        FriendRequest request = new FriendRequest();
        request.setRequesterId(requesterId);
        request.setRecipientId(recipientId);
        request.setRequestMessage(dto.getRequestMessage());
        request.setStatus("PENDING");
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        
        friendRequestService.save(request);
        
        // 发送通知
        UserVO requester = userService.getUserVOById(requesterId);
        
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setSenderId(requesterId);
        notification.setType("FRIEND_REQUEST");
        notification.setTitle("新的好友申请");
        notification.setContent(requester.getNickname() + "向你发送了好友申请");
        notification.setIsRead(false);
        notification.setRelatedId(request.getRequestId());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        
        notificationService.save(notification);
        
        // 发送WebSocket消息
        UserWebSocketHandler.sendFriendRequestNotification(recipientId, requesterId, requester.getNickname(), request.getRequestId());
        
        return request.getRequestId();
    }
    
    @Override
    @Transactional
    public boolean acceptFriendRequest(Long userId, Long requestId) {
        // 查询申请
        FriendRequest request = friendRequestService.getById(requestId);
        if (request == null) {
            throw new BusinessException("好友申请不存在");
        }
        
        // 验证接收者是当前用户
        if (!userId.equals(request.getRecipientId())) {
            throw new BusinessException("无权操作此好友申请");
        }
        
        // 验证申请状态
        if (!"PENDING".equals(request.getStatus())) {
            throw new BusinessException("该申请已被处理");
        }
        
        // 更新申请状态
        request.setStatus("ACCEPTED");
        request.setUpdatedAt(LocalDateTime.now());
        friendRequestService.updateById(request);
        
        // 创建好友关系（双向关系）
        Friend friend1 = new Friend();
        friend1.setUserId(userId);
        friend1.setFriendId(request.getRequesterId());
        friend1.setCreatedAt(LocalDateTime.now());
        
        Friend friend2 = new Friend();
        friend2.setUserId(request.getRequesterId());
        friend2.setFriendId(userId);
        friend2.setCreatedAt(LocalDateTime.now());
        
        this.save(friend1);
        this.save(friend2);
        
        // 发送通知给申请者
        UserVO recipient = userService.getUserVOById(userId);
        
        Notification notification = new Notification();
        notification.setRecipientId(request.getRequesterId());
        notification.setSenderId(userId);
        notification.setType("FRIEND_REQUEST_ACCEPTED");
        notification.setTitle("好友申请已接受");
        notification.setContent(recipient.getNickname() + "接受了你的好友申请");
        notification.setIsRead(false);
        notification.setRelatedId(requestId);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        
        notificationService.save(notification);
        
        // 发送WebSocket消息
        UserWebSocketHandler.sendFriendRequestStatusNotification(request.getRequesterId(), userId, recipient.getNickname(), "ACCEPTED");
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean rejectFriendRequest(Long userId, Long requestId) {
        // 查询申请
        FriendRequest request = friendRequestService.getById(requestId);
        if (request == null) {
            throw new BusinessException("好友申请不存在");
        }
        
        // 验证接收者是当前用户
        if (!userId.equals(request.getRecipientId())) {
            throw new BusinessException("无权操作此好友申请");
        }
        
        // 验证申请状态
        if (!"PENDING".equals(request.getStatus())) {
            throw new BusinessException("该申请已被处理");
        }
        
        // 更新申请状态
        request.setStatus("REJECTED");
        request.setUpdatedAt(LocalDateTime.now());
        friendRequestService.updateById(request);
        
        // 发送通知给申请者
        UserVO recipient = userService.getUserVOById(userId);
        
        Notification notification = new Notification();
        notification.setRecipientId(request.getRequesterId());
        notification.setSenderId(userId);
        notification.setType("FRIEND_REQUEST_REJECTED");
        notification.setTitle("好友申请被拒绝");
        notification.setContent(recipient.getNickname() + "拒绝了你的好友申请");
        notification.setIsRead(false);
        notification.setRelatedId(requestId);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        
        notificationService.save(notification);
        
        // 发送WebSocket消息
        UserWebSocketHandler.sendFriendRequestStatusNotification(request.getRequesterId(), userId, recipient.getNickname(), "REJECTED");
        
        return true;
    }
    
    @Override
    public List<FriendVO> getFriendList(Long userId) {
        // 使用自定义查询获取好友信息（已包含用户信息）
        List<Map<String, Object>> friendMaps = this.baseMapper.getFriendsWithUserInfo(userId);
        
        List<FriendVO> result = new ArrayList<>();
        for (Map<String, Object> map : friendMaps) {
            FriendVO vo = new FriendVO();
            vo.setId((Long) map.get("id"));
            vo.setFriendId((Long) map.get("friend_id"));
            
            // 构建好友用户信息
            UserVO friendUser = new UserVO();
            friendUser.setUserId((Long) map.get("user_id"));
            friendUser.setUsername((String) map.get("username"));
            friendUser.setNickname((String) map.get("nickname"));
            friendUser.setAvatarUrl((String) map.get("avatar_url"));
            friendUser.setGender((String) map.get("gender"));
            friendUser.setMajor((String) map.get("major"));
            friendUser.setGrade((String) map.get("grade"));
            
            vo.setFriend(friendUser);
            result.add(vo);
        }
        
        return result;
    }
    
    @Override
    public List<FriendRequestVO> getFriendRequests(Long userId) {
        // 查询接收到的好友申请
        LambdaQueryWrapper<FriendRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRequest::getRecipientId, userId)
                .eq(FriendRequest::getStatus, "PENDING")
                .orderByDesc(FriendRequest::getCreatedAt);
        
        List<FriendRequest> requests = friendRequestService.list(wrapper);
        
        return friendRequestService.buildRequestVOList(requests);
    }
    
    @Override
    @Transactional
    public boolean deleteFriend(Long userId, Long friendId) {
        // 删除双向好友关系
        LambdaQueryWrapper<Friend> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId);
        
        LambdaQueryWrapper<Friend> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(Friend::getUserId, friendId)
                .eq(Friend::getFriendId, userId);
        
        boolean result1 = this.remove(wrapper1);
        boolean result2 = this.remove(wrapper2);
        
        return result1 && result2;
    }
    
    @Override
    public Page<FriendVO> searchFriends(Long userId, String keyword, Integer page, Integer size) {
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 使用自定义查询搜索好友
        List<Map<String, Object>> friendMaps = this.baseMapper.searchFriends(userId, keyword, offset, size);
        Integer total = this.baseMapper.countSearchFriends(userId, keyword);
        
        // 构建FriendVO列表
        List<FriendVO> friendVOs = new ArrayList<>();
        for (Map<String, Object> map : friendMaps) {
            FriendVO vo = new FriendVO();
            vo.setId((Long) map.get("id"));
            vo.setFriendId((Long) map.get("friend_id"));
            
            // 构建好友用户信息
            UserVO friendUser = new UserVO();
            friendUser.setUserId((Long) map.get("user_id"));
            friendUser.setUsername((String) map.get("username"));
            friendUser.setNickname((String) map.get("nickname"));
            friendUser.setAvatarUrl((String) map.get("avatar_url"));
            friendUser.setGender((String) map.get("gender"));
            friendUser.setMajor((String) map.get("major"));
            friendUser.setGrade((String) map.get("grade"));
            
            vo.setFriend(friendUser);
            friendVOs.add(vo);
        }
        
        // 构建分页结果
        Page<FriendVO> result = new Page<>(page, size);
        result.setTotal(total != null ? total : 0);
        result.setRecords(friendVOs);
        
        return result;
    }
    
    @Override
    public boolean isFriend(Long userId, Long friendId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId);
        
        return this.count(wrapper) > 0;
    }
    
    @Override
    public List<Long> getFriendIds(Long userId) {
        return this.baseMapper.selectFriendIdsByUserId(userId);
    }
    
    @Override
    public String getFriendRequestStatus(Long userId, Long otherUserId) {
        // 查询从当前用户发送给对方的申请
        LambdaQueryWrapper<FriendRequest> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(FriendRequest::getRequesterId, userId)
                .eq(FriendRequest::getRecipientId, otherUserId)
                .orderByDesc(FriendRequest::getCreatedAt)
                .last("LIMIT 1");
        
        FriendRequest outgoingRequest = friendRequestService.getOne(wrapper1);
        
        // 查询从对方发送给当前用户的申请
        LambdaQueryWrapper<FriendRequest> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(FriendRequest::getRequesterId, otherUserId)
                .eq(FriendRequest::getRecipientId, userId)
                .orderByDesc(FriendRequest::getCreatedAt)
                .last("LIMIT 1");
        
        FriendRequest incomingRequest = friendRequestService.getOne(wrapper2);
        
        // 优先返回PENDING状态
        if (outgoingRequest != null && "PENDING".equals(outgoingRequest.getStatus())) {
            return "PENDING_OUTGOING"; // 发出的申请待处理
        }
        
        if (incomingRequest != null && "PENDING".equals(incomingRequest.getStatus())) {
            return "PENDING_INCOMING"; // 收到的申请待处理
        }
        
        // 如果有已接受的申请，则应该已经是好友了，会在isFriend中判断
        
        // 其他情况返回最近的一条申请状态
        if (outgoingRequest != null) {
            return outgoingRequest.getStatus() + "_OUTGOING";
        }
        
        if (incomingRequest != null) {
            return incomingRequest.getStatus() + "_INCOMING";
        }
        
        return null; // 无申请记录
    }
}
