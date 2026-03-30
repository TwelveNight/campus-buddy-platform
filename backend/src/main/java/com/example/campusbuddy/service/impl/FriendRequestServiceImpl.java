package com.example.campusbuddy.service.impl;

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
}

