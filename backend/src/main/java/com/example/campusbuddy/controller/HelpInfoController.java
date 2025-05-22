package com.example.campusbuddy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusbuddy.common.R;
import com.example.campusbuddy.common.ResultCode;
import com.example.campusbuddy.dto.HelpInfoDTO;
import com.example.campusbuddy.entity.HelpInfo;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.exception.ForbiddenException;
import com.example.campusbuddy.exception.InvalidParameterException;
import com.example.campusbuddy.exception.ResourceNotFoundException;
import com.example.campusbuddy.exception.UnauthorizedException;
import com.example.campusbuddy.mapper.UserMapper;
import com.example.campusbuddy.mapper.UserMapper;
import com.example.campusbuddy.service.HelpInfoService;
import com.example.campusbuddy.vo.HelpInfoDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/helpinfo")
@Tag(name = "HelpInfo", description = "互助信息相关接口")
public class HelpInfoController {
    @Autowired
    private HelpInfoService helpInfoService;

    @Autowired
    private UserMapper userMapper;

    @PostMapping
    @Operation(summary = "发布互助信息")
    public R<HelpInfo> create(@Valid @RequestBody HelpInfoDTO helpInfoDTO, HttpServletRequest request) {
        // 从认证信息中获取用户ID
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new UnauthorizedException();
        }

        // DTO转换为实体
        HelpInfo helpInfo = new HelpInfo();
        BeanUtils.copyProperties(helpInfoDTO, helpInfo);

        // 设置发布者ID为当前登录用户
        helpInfo.setPublisherId(userId);
        // 设置初始状态为"开放"
        helpInfo.setStatus("OPEN");
        // 设置初始浏览量为0
        helpInfo.setViewCount(0);

        helpInfoService.save(helpInfo);
        return R.ok("互助信息发布成功", helpInfo);
    }

    @GetMapping
    @Operation(summary = "分页查询互助信息列表")
    public R<IPage<HelpInfo>> list(@RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String publisherId,
            HttpServletRequest request) {
        QueryWrapper<HelpInfo> wrapper = new QueryWrapper<>();
        if (type != null)
            wrapper.eq("type", type);
        if (status != null)
            wrapper.eq("status", status);

        // 处理 publisherId 参数，如果为 'my' 则表示获取当前用户发布的互助信息
        if (publisherId != null) {
            if ("my".equals(publisherId)) {
                // 从认证信息中获取当前用户ID
                Long userId = (Long) request.getAttribute("userId");
                if (userId == null) {
                    throw new UnauthorizedException();
                }
                wrapper.eq("publisher_id", userId);
            } else {
                // 如果不是 'my'，则可能是其他用户ID
                try {
                    Long pubId = Long.parseLong(publisherId);
                    wrapper.eq("publisher_id", pubId);
                } catch (NumberFormatException e) {
                    throw new InvalidParameterException("发布者ID格式不正确");
                }
            }
        }

        IPage<HelpInfo> result = helpInfoService.page(new Page<>(page, size), wrapper);

        // 处理结果，添加发布者名称信息
        for (HelpInfo info : result.getRecords()) {
            User publisher = userMapper.selectById(info.getPublisherId());
            if (publisher != null) {
                info.getParams().put("publisherName", publisher.getNickname());
                info.getParams().put("publisherAvatar", publisher.getAvatarUrl());
            }
        }

        return R.ok("获取互助信息列表成功", result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查看互助信息详情")
    public R<HelpInfoDetailVO> detail(@PathVariable Long id) {
        HelpInfoDetailVO info = helpInfoService.getHelpInfoDetail(id);
        return R.ok("获取互助信息详情成功", info);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新互助信息")
    public R<HelpInfo> update(@PathVariable Long id, @Valid @RequestBody HelpInfoDTO helpInfoDTO,
            HttpServletRequest request) {
        // 从认证信息中获取用户ID
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new UnauthorizedException();
        }

        // 检查互助信息是否存在
        HelpInfo existingInfo = helpInfoService.getById(id);
        if (existingInfo == null) {
            throw new ResourceNotFoundException("互助信息", id);
        }

        // 检查是否是发布者本人在更新
        if (!existingInfo.getPublisherId().equals(userId)) {
            throw new ForbiddenException("只有发布者才能更新互助信息");
        }

        // 更新互助信息
        BeanUtils.copyProperties(helpInfoDTO, existingInfo);
        existingInfo.setInfoId(id); // 确保ID不变
        existingInfo.setPublisherId(userId); // 确保发布者ID不变

        helpInfoService.updateById(existingInfo);
        return R.ok("互助信息更新成功", existingInfo);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除互助信息")
    public R<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        // 从认证信息中获取用户ID
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new UnauthorizedException();
        }

        // 检查互助信息是否存在
        HelpInfo existingInfo = helpInfoService.getById(id);
        if (existingInfo == null) {
            throw new ResourceNotFoundException("互助信息", id);
        }

        // 检查权限：只有发布者本人或管理员可以删除
        if (!existingInfo.getPublisherId().equals(userId)) {
            // 获取用户角色列表
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) request.getAttribute("roles");
            boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");

            if (!isAdmin) {
                throw new ForbiddenException("只有发布者或管理员才能删除互助信息");
            }
        }

        helpInfoService.removeById(id);
        return R.ok("互助信息删除成功", null);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新互助信息状态")
    public R<HelpInfo> updateStatus(@PathVariable Long id,
            @RequestParam String status,
            HttpServletRequest request) {
        // 从认证信息中获取用户ID
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new UnauthorizedException();
        }

        // 检查互助信息是否存在
        HelpInfo existingInfo = helpInfoService.getById(id);
        if (existingInfo == null) {
            throw new ResourceNotFoundException("互助信息", id);
        }

        // 检查是否是发布者本人在更新状态
        if (!existingInfo.getPublisherId().equals(userId)) {
            throw new ForbiddenException("只有发布者才能更新互助信息状态");
        }

        // 验证状态值是否合法
        if (!isValidStatus(status)) {
            throw new InvalidParameterException("无效的状态值");
        }

        // 更新状态
        existingInfo.setStatus(status);

        // 如果状态变为OPEN，清除已接受的申请ID
        if ("OPEN".equals(status)) {
            existingInfo.setAcceptedApplicationId(null);
        }

        helpInfoService.updateById(existingInfo);

        return R.ok("互助信息状态更新成功", existingInfo);
    }

    // 验证状态值是否合法
    private boolean isValidStatus(String status) {
        return status != null && (status.equals("OPEN") ||
                status.equals("IN_PROGRESS") ||
                status.equals("RESOLVED") ||
                status.equals("EXPIRED") ||
                status.equals("CLOSED"));
    }

    @PatchMapping("/{id}/view")
    @Operation(summary = "增加互助信息浏览量")
    public R<HelpInfo> incrementViewCount(@PathVariable Long id) {
        HelpInfo info = helpInfoService.incrementViewCount(id);
        return R.ok("浏览量更新成功", info);
    }
}
