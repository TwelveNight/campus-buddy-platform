package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.entity.FriendRequest;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.mapper.FriendRequestMapper;
import com.example.campusbuddy.service.FriendRequestService;
import com.example.campusbuddy.service.UserService;
import com.example.campusbuddy.vo.FriendRequestVO;
import com.example.campusbuddy.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 好友申请服务实现类
 */
@Service
public class FriendRequestServiceImpl extends ServiceImpl<FriendRequestMapper, FriendRequest> implements FriendRequestService {
    
    @Autowired
    private UserService userService;
    
    @Override
    public FriendRequestVO buildRequestVO(FriendRequest request) {
        if (request == null) {
            return null;
        }
        
        FriendRequestVO vo = new FriendRequestVO();
        vo.setRequestId(request.getRequestId());
        vo.setRequesterId(request.getRequesterId());
        vo.setRecipientId(request.getRecipientId());
        vo.setRequestMessage(request.getRequestMessage());
        vo.setStatus(request.getStatus());
        vo.setCreatedAt(request.getCreatedAt());
        
        // 设置申请者和接收者信息
        UserVO requester = userService.getUserVOById(request.getRequesterId());
        UserVO recipient = userService.getUserVOById(request.getRecipientId());
        vo.setRequester(requester);
        vo.setRecipient(recipient);
        
        return vo;
    }
    
    @Override
    public List<FriendRequestVO> buildRequestVOList(List<FriendRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 收集所有用户ID
        List<Long> userIds = new ArrayList<>();
        for (FriendRequest request : requests) {
            userIds.add(request.getRequesterId());
            userIds.add(request.getRecipientId());
        }
        
        // 批量获取用户信息
        List<User> users = userService.listByIds(userIds);
        Map<Long, UserVO> userMap = users.stream()
                .collect(Collectors.toMap(
                        User::getUserId,
                        user -> {
                            UserVO vo = new UserVO();
                            vo.setUserId(user.getUserId());
                            vo.setUsername(user.getUsername());
                            vo.setNickname(user.getNickname());
                            vo.setAvatarUrl(user.getAvatarUrl());
                            vo.setGender(user.getGender());
                            return vo;
                        }
                ));
        
        // 构建VO列表
        List<FriendRequestVO> vos = new ArrayList<>();
        for (FriendRequest request : requests) {
            FriendRequestVO vo = new FriendRequestVO();
            vo.setRequestId(request.getRequestId());
            vo.setRequesterId(request.getRequesterId());
            vo.setRecipientId(request.getRecipientId());
            vo.setRequestMessage(request.getRequestMessage());
            vo.setStatus(request.getStatus());
            vo.setCreatedAt(request.getCreatedAt());
            
            // 设置用户信息
            vo.setRequester(userMap.get(request.getRequesterId()));
            vo.setRecipient(userMap.get(request.getRecipientId()));
            
            vos.add(vo);
        }
        
        return vos;
    }
    
    /**
     * 根据接收者ID和状态获取好友申请列表（使用XML查询）
     */
    public List<FriendRequestVO> getRequestsByRecipientAndStatus(Long recipientId, String status) {
        List<Map<String, Object>> requestMaps = this.baseMapper.getRequestsWithUserInfo(recipientId, status);
        
        List<FriendRequestVO> vos = new ArrayList<>();
        for (Map<String, Object> map : requestMaps) {
            FriendRequestVO vo = new FriendRequestVO();
            vo.setRequestId((Long) map.get("request_id"));
            vo.setRequesterId((Long) map.get("requester_id"));
            vo.setRecipientId((Long) map.get("recipient_id"));
            vo.setRequestMessage((String) map.get("request_message"));
            vo.setStatus((String) map.get("status"));
            vo.setCreatedAt((LocalDateTime) map.get("created_at"));
            
            // 构建申请者信息
            UserVO requester = new UserVO();
            requester.setUserId((Long) map.get("user_id"));
            requester.setUsername((String) map.get("username"));
            requester.setNickname((String) map.get("nickname"));
            requester.setAvatarUrl((String) map.get("avatar_url"));
            requester.setGender((String) map.get("gender"));
            
            vo.setRequester(requester);
            vos.add(vo);
        }
        
        return vos;
    }
}
