package com.example.campusbuddy.controller;

import com.example.campusbuddy.common.R;
import com.example.campusbuddy.entity.HelpApplication;
import com.example.campusbuddy.entity.HelpInfo;
import com.example.campusbuddy.exception.ForbiddenException;
import com.example.campusbuddy.service.HelpApplicationService;
import com.example.campusbuddy.service.HelpInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "HelpApplication", description = "互助申请相关接口")
@RestController
@RequestMapping("/api/help-application")
public class HelpApplicationController {
    @Autowired
    private HelpApplicationService helpApplicationService;

    @Autowired
    private HelpInfoService helpInfoService;

    @Operation(summary = "提交互助申请")
    @PostMapping
    public R<HelpApplication> apply(@RequestBody HelpApplication application, HttpServletRequest request) {
        // 获取当前用户ID
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null)
            return R.unauthorized();

        // 获取互助信息
        Long helpInfoId = application.getInfoId();
        HelpInfo helpInfo = helpInfoService.getById(helpInfoId);
        if (helpInfo == null) {
            return R.fail("互助信息不存在");
        }

        // 检查是否是申请自己发布的互助信息
        if (userId.equals(helpInfo.getPublisherId())) {
            return R.fail("不能申请自己发布的互助信息");
        }

        // 检查互助信息是否处于"开放"状态
        if (!"OPEN".equals(helpInfo.getStatus())) {
            return R.fail("该互助信息当前状态不允许申请");
        }
        
        // 检查是否已经有被接受的申请
        if (helpInfo.getAcceptedApplicationId() != null) {
            return R.fail("该互助信息已有被接受的申请");
        }

        // 检查用户是否已有正在处理中或已被接受的申请
        long activeApplicationCount = helpApplicationService.lambdaQuery()
                .eq(HelpApplication::getInfoId, helpInfoId)
                .eq(HelpApplication::getApplicantId, userId)
                .in(HelpApplication::getStatus, "PENDING", "ACCEPTED") // 只检查PENDING或ACCEPTED状态的申请
                .count();
        if (activeApplicationCount > 0) {
            return R.fail("您已经提交过申请，且该申请正在处理中或已被接受。");
        }

        // 设置申请者ID和初始状态
        application.setApplicantId(userId);
        application.setPublisherId(helpInfo.getPublisherId()); // 设置发布者ID，便于后续查询
        application.setStatus("PENDING");

        helpApplicationService.save(application);
        return R.ok("申请提交成功", application);
    }

    @Operation(summary = "获取我发出的申请")
    @GetMapping("/my")
    public R<List<HelpApplication>> myApplications(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null)
            return R.unauthorized();
        List<HelpApplication> list = helpApplicationService.lambdaQuery()
                .eq(HelpApplication::getApplicantId, userId).list();
        return R.ok("获取成功", list);
    }

    @Operation(summary = "获取我收到的申请")
    @GetMapping("/received")
    public R<List<HelpApplication>> receivedApplications(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null)
            return R.unauthorized();
        List<HelpApplication> list = helpApplicationService.lambdaQuery()
                .eq(HelpApplication::getPublisherId, userId).list();
        return R.ok("获取成功", list);
    }

    /**
     * 更新申请状态（接受/拒绝）
     */
    @Operation(summary = "更新申请状态")
    @PutMapping("/{applicationId}/status")
    public R<HelpApplication> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestBody HelpApplication updateRequest,
            HttpServletRequest request) {

        // 获取当前用户ID
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.unauthorized();
        }

        // 获取申请信息
        HelpApplication application = helpApplicationService.getById(applicationId);
        if (application == null) {
            return R.fail("申请信息不存在");
        }

        // 获取请求中的新状态
        String newStatus = updateRequest.getStatus();

        // 处理取消申请的情况：申请人可以取消自己的申请
        if ("CANCELED".equals(newStatus)) {
            if (userId.equals(application.getApplicantId())) {
                // 允许用户取消自己的申请
                application.setStatus(newStatus);
                helpApplicationService.updateById(application);

                // 如果取消的是已接受的申请，则需要清除HelpInfo的acceptedApplicationId字段，并将状态设为OPEN
                HelpInfo helpInfo = helpInfoService.getById(application.getInfoId());
                if (helpInfo != null && helpInfo.getAcceptedApplicationId() != null
                        && helpInfo.getAcceptedApplicationId().equals(applicationId)) {
                    helpInfo.setAcceptedApplicationId(null);
                    helpInfo.setStatus("OPEN");
                    helpInfoService.updateById(helpInfo);
                }
                return R.ok("申请已取消", application);
            } else {
                // 如果不是申请人自己取消，则需要发布者权限
                if (!userId.equals(application.getPublisherId())) {
                    return R.fail("只有发布者或申请人才能取消申请");
                }
                // 如果是发布者取消一个非自己申请的CANCELED状态（理论上不应该发生），按兵不动或按需处理
                // 此处我们假设发布者不能直接将一个别人的申请标记为CANCELED，除非这个申请已被接受然后发布者想撤销合作
                // 但撤销合作的逻辑通常是改变HelpInfo的状态，而不是直接改Application的状态为CANCELED
                // 为简化，此处若为发布者操作CANCELED，且非申请人，则拒绝
                return R.fail("发布者不能直接取消他人的申请，除非是撤销已接受的合作（这应通过其他接口）");
            }
        }

        // 对于非取消操作（如接受、拒绝），检查是否是互助信息的发布者
        if (!userId.equals(application.getPublisherId())) {
            return R.fail("只有发布者才能执行此操作");
        }

        // 对于非取消操作，检查是否是待处理状态
        if (!"PENDING".equals(application.getStatus())) {
            return R.fail("只能更新待处理的申请");
        }
        
        // 验证状态是否合法（只能是ACCEPTED或REJECTED）
        if (newStatus == null || (!newStatus.equals("ACCEPTED") && !newStatus.equals("REJECTED"))) {
            return R.fail("无效的申请状态，只能接受(ACCEPTED)或拒绝(REJECTED)");
        }

        // 更新申请状态
        application.setStatus(newStatus);
        helpApplicationService.updateById(application);

        // 如果是接受申请，则将互助信息状态更新为"处理中"，并记录已接受的申请ID
        if ("ACCEPTED".equals(newStatus)) {
            HelpInfo helpInfo = helpInfoService.getById(application.getInfoId());
            if (helpInfo != null) {
                // 更新状态为处理中并记录已接受的申请ID
                helpInfo.setStatus("IN_PROGRESS");
                helpInfo.setAcceptedApplicationId(applicationId);
                helpInfoService.updateById(helpInfo);
            }

            // 拒绝该互助信息下的其他待处理申请
            helpApplicationService.lambdaUpdate()
                    .eq(HelpApplication::getInfoId, application.getInfoId())
                    .eq(HelpApplication::getStatus, "PENDING")
                    .ne(HelpApplication::getApplicationId, applicationId)
                    .set(HelpApplication::getStatus, "REJECTED")
                    .update();
        }

        return R.ok("申请状态更新成功", application);
    }

    /**
     * 获取特定互助信息的申请列表
     */
    @Operation(summary = "获取互助信息的申请列表")
    @GetMapping
    public R<List<HelpApplication>> getApplicationsByHelpInfoId(
            @RequestParam Long infoId,
            HttpServletRequest request) {

        // 获取当前用户ID
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.unauthorized();
        }

        // 获取互助信息
        HelpInfo helpInfo = helpInfoService.getById(infoId);
        if (helpInfo == null) {
            return R.fail("互助信息不存在");
        }

        // 只有发布者可以查看申请列表
        if (!userId.equals(helpInfo.getPublisherId())) {
            return R.fail("只有发布者才能查看申请列表");
        }

        List<HelpApplication> applications = helpApplicationService.lambdaQuery()
                .eq(HelpApplication::getInfoId, infoId)
                .list();

        return R.ok("获取成功", applications);
    }
}
