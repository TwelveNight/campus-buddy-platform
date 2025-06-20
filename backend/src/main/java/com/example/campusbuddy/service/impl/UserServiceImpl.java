package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.dto.LoginDTO;
import com.example.campusbuddy.dto.PasswordUpdateDTO;
import com.example.campusbuddy.dto.ProfileUpdateDTO;
import com.example.campusbuddy.dto.RegisterDTO;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.mapper.UserMapper;
import com.example.campusbuddy.security.JwtUtil;
import com.example.campusbuddy.service.UserCacheService;
import com.example.campusbuddy.service.UserRoleService;
import com.example.campusbuddy.service.UserService;
import com.example.campusbuddy.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleService userRoleService;
    
    @Autowired
    private UserCacheService userCacheService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public UserVO register(RegisterDTO dto) {
        // 检查用户名是否已存在
        if (userMapper.selectOne(new QueryWrapper<User>().eq("username", dto.getUsername())) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setCreditScore(100);
        user.setStatus("ACTIVE");
        this.save(user);

        // 为新用户分配默认角色
        userRoleService.addRoleToUser(user.getUserId(), "ROLE_USER");

        UserVO userVO = getUserVOById(user.getUserId());
        
        // 缓存新用户信息
        userCacheService.cacheUser(user);
        userCacheService.cacheUserVO(userVO);
        
        log.info("用户注册成功: userId={}, username={}", user.getUserId(), user.getUsername());
        
        return userVO;
    }

    @Override
    public String login(LoginDTO dto) {
        // 先尝试从缓存获取用户信息
        User user = userCacheService.getUserByUsernameFromCache(dto.getUsername());
        if (user == null) {
            user = userMapper.selectOne(new QueryWrapper<User>().eq("username", dto.getUsername()));
            if (user != null) {
                // 缓存用户信息
                userCacheService.cacheUser(user);
            }
        }
        
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        
        // 检查用户状态
        if ("BANNED".equals(user.getStatus())) {
            throw new IllegalArgumentException("该账号已被禁用，请联系管理员");
        }
        if ("INACTIVE".equals(user.getStatus())) {
            throw new IllegalArgumentException("该账号未激活，请联系管理员");
        }
        
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());
        
        // 缓存登录 token (设置较短的过期时间，如 2 小时)
        userCacheService.cacheUserToken(user.getUserId(), token, 7200);
        
        return token;
    }

    @Override
    public UserVO getUserVOById(Long userId) {
        // 先尝试从缓存获取
        UserVO cachedUserVO = userCacheService.getUserVOFromCache(userId);
        if (cachedUserVO != null) {
            return cachedUserVO;
        }
        
        User user = userMapper.selectById(userId);
        if (user == null)
            return null;
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        
        // 获取用户角色信息并设置到VO对象中
        List<String> roles = userRoleService.getUserRoles(userId);
        vo.setRoles(roles);
        
        // 缓存用户信息
        userCacheService.cacheUserVO(vo);
        
        return vo;
    }

    @Override
    public UserVO updateProfile(Long userId, ProfileUpdateDTO dto) {
        User user = getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 更新用户信息
        if (dto.getNickname() != null && !dto.getNickname().isEmpty()) {
            user.setNickname(dto.getNickname());
        }

        if (dto.getAvatarUrl() != null) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }
        
        // 更新新增字段
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
        
        if (dto.getMajor() != null) {
            user.setMajor(dto.getMajor());
        }
        
        if (dto.getGrade() != null) {
            user.setGrade(dto.getGrade());
        }
        
        if (dto.getContactInfo() != null) {
            user.setContactInfo(dto.getContactInfo());
        }
        
        if (dto.getSkillTags() != null) {
            user.setSkillTags(dto.getSkillTags());
        }

        // 保存更新
        this.updateById(user);

        // 清除缓存
        userCacheService.evictUserCache(userId);

        // 返回更新后的用户信息
        return getUserVOById(userId);
    }

    @Override
    public void updatePassword(Long userId, PasswordUpdateDTO dto) {
        User user = getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("原密码错误");
        }

        // 更新密码
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        this.updateById(user);
        
        // 清除缓存
        userCacheService.evictUserCache(userId);
    }
    
    @Override
    public User getUserByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }
    
    @Override
    public boolean isAdmin(Long userId) {
        return userRoleService.hasRole(userId, "ROLE_ADMIN");
    }
    
    @Override
    public boolean exists(Long userId) {
        return getById(userId) != null;
    }
    
    @Override
    public Page<UserVO> searchUsers(String keyword, Integer page, Integer size) {
        // 先尝试从缓存获取搜索结果
        Page<UserVO> cachedResult = userCacheService.getSearchResultFromCache(keyword, page, size);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        // 创建分页对象
        Page<User> userPage = new Page<>(page, size);
        
        // 构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("username", keyword)
                    .or()
                    .like("nickname", keyword)
                    .or()
                    .like("contact_info", keyword);
        }
        
        // 只查询激活状态的用户
        queryWrapper.eq("status", "ACTIVE");
        
        // 执行分页查询
        Page<User> result = this.page(userPage, queryWrapper);
        
        // 转换为VO对象
        Page<UserVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<UserVO> voList = new ArrayList<>();
        for (User user : result.getRecords()) {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            // 设置角色信息
            List<String> roles = userRoleService.getUserRoles(user.getUserId());
            vo.setRoles(roles);
            voList.add(vo);
        }
        voPage.setRecords(voList);
        
        // 缓存搜索结果 (设置较短的过期时间，如 10 分钟)
        userCacheService.cacheSearchResult(keyword, page, size, voPage, 600);
        
        return voPage;
    }

    @Override
    public Page<UserVO> adminPageUsers(Integer pageNum, Integer pageSize, String keyword, String status) {
        Page<User> userPage = new Page<>(pageNum, pageSize);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like("username", keyword)
                    .or()
                    .like("nickname", keyword)
                    .or()
                    .like("contact_info", keyword)); // Changed from email to contact_info
        }

        if (status != null && !status.trim().isEmpty()) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("created_at");

        Page<User> resultPage = this.page(userPage, queryWrapper);

        Page<UserVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<UserVO> voList = new ArrayList<>();
        for (User user : resultPage.getRecords()) {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            List<String> roles = userRoleService.getUserRoles(user.getUserId());
            vo.setRoles(roles);
            voList.add(vo);
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional
    public boolean adminUpdateUserStatus(Long userId, String status) {
        User user = this.getById(userId);
        if (user == null) return false;
        user.setStatus(status);
        boolean result = this.updateById(user);
        
        // 清除缓存
        if (result) {
            userCacheService.evictUserCache(userId);
            if ("BANNED".equals(status)) {
                userCacheService.evictUserToken(userId);
            }
            // 启用账户时，刷新用户缓存到Redis
            if ("ACTIVE".equals(status)) {
                userCacheService.cacheUser(user);
            }
        }
        
        return result;
    }

    @Override
    @Transactional
    public String adminResetPassword(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        // 生成一个随机的新密码
        String newPassword = generateRandomPassword(8);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        this.updateById(user);
        
        // 清除缓存
        userCacheService.evictUserCache(userId);
        
        return newPassword;
    }

    private String generateRandomPassword(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length]; // 生成更短的字节数组以获得更短的Base64字符串
        random.nextBytes(bytes);
        // 使用URL和文件名安全的Base64编码，并移除填充字符'='
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, length);
    }
}
