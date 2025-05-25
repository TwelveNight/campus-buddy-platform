package com.example.campusbuddy.controller;

import com.example.campusbuddy.common.R;
import com.example.campusbuddy.entity.HelpApplication;
import com.example.campusbuddy.entity.HelpInfo;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.service.HelpApplicationService;
import com.example.campusbuddy.service.HelpInfoService;
import com.example.campusbuddy.service.NotificationService;
import com.example.campusbuddy.service.UserService;
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
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;

    @Operation(summary = "提交互助申请")
    @PostMapping
    public R<HelpApplication> apply(@RequestBody HelpApplication application, HttpServletRequest request) {
        // 获取当前用户ID
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null)
            return R.unauthorized();

        // 获取互助任务
        Long helpInfoId = application.getInfoId();
        HelpInfo helpInfo = helpInfoService.getById(helpInfoId);
        if (helpInfo == null) {
            return R.fail("互助任务不存在");
        }

        // 检查是否是申请自己发布的互助任务
        if (userId.equals(helpInfo.getPublisherId())) {
            return R.fail("不能申请自己发布的互助任务");
        }

        // 检查互助任务是否处于"开放"状态
        if (!"OPEN".equals(helpInfo.getStatus())) {
            return R.fail("该互助任务当前状态不允许申请");
        }

        // 检查是否已经有被接受的申请
        if (helpInfo.getAcceptedApplicationId() != null) {
            return R.fail("该互助任务已有被接受的申请");
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
        
        // 获取申请者信息，发送通知给互助任务发布者
        User applicant = userService.getById(userId);
        if (applicant != null) {
            notificationService.createHelpApplicationNotification(
                helpInfoId, 
                helpInfo.getPublisherId(), 
                userId, 
                applicant.getNickname()
            );
        }
        
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

        // 对于非取消操作（如接受、拒绝），检查是否是互助任务的发布者
        if (!userId.equals(application.getPublisherId())) {
            return R.fail("只有发布者才能执行此操作");
        }

        // 对于非取消操作，检查是否是待处理状态或可被拒绝的已接受状态
        if (!"PENDING".equals(application.getStatus())) {
            // 如果目标状态是REJECTED，且当前状态是ACCEPTED，也允许操作（用于取消合作后拒绝原申请）
            if (!("REJECTED".equals(newStatus) && "ACCEPTED".equals(application.getStatus()))) {
                return R.fail("只能更新待处理的申请，或将已接受的申请更新为已拒绝");
            }
        }

        // 验证状态是否合法（只能是ACCEPTED或REJECTED）
        if (newStatus == null || (!newStatus.equals("ACCEPTED") && !newStatus.equals("REJECTED"))) {
            return R.fail("无效的申请状态，只能接受(ACCEPTED)或拒绝(REJECTED)");
        }

        // 更新申请状态
        application.setStatus(newStatus);
        helpApplicationService.updateById(application);

        // 获取当前操作者信息（用于通知）
        User operator = userService.getById(userId);
        String operatorName = (operator != null) ? operator.getNickname() : "系统管理员";

        HelpInfo helpInfo = helpInfoService.getById(application.getInfoId());
        if (helpInfo != null) {
            if ("ACCEPTED".equals(newStatus)) {
                // 更新状态为处理中并记录已接受的申请ID
                helpInfo.setStatus("IN_PROGRESS");
                helpInfo.setAcceptedApplicationId(applicationId);
                helpInfoService.updateById(helpInfo);

                // 发送申请被接受的通知
                notificationService.createApplicationResultNotification(
                    helpInfo.getInfoId(),
                    application.getApplicantId(),
                    true,
                    helpInfo.getTitle(),
                    userId,
                    operatorName
                );

                // 拒绝该互助任务下的其他待处理申请
                helpApplicationService.lambdaUpdate()
                        .eq(HelpApplication::getInfoId, application.getInfoId())
                        .eq(HelpApplication::getStatus, "PENDING")
                        .ne(HelpApplication::getApplicationId, applicationId)
                        .set(HelpApplication::getStatus, "REJECTED")
                        .update();
                        
                // 向被拒绝的申请者发送通知
                List<HelpApplication> rejectedApplications = helpApplicationService.lambdaQuery()
                        .eq(HelpApplication::getInfoId, application.getInfoId())
                        .eq(HelpApplication::getStatus, "REJECTED")
                        .ne(HelpApplication::getApplicationId, applicationId)
                        .list();
                        
                for (HelpApplication rejectedApp : rejectedApplications) {
                    notificationService.createApplicationResultNotification(
                        helpInfo.getInfoId(),
                        rejectedApp.getApplicantId(),
                        false,
                        helpInfo.getTitle(),
                        userId,
                        operatorName
                    );
                }
            } else if ("REJECTED".equals(newStatus)) {
                // 发送申请被拒绝的通知
                notificationService.createApplicationResultNotification(
                    helpInfo.getInfoId(),
                    application.getApplicantId(),
                    false,
                    helpInfo.getTitle(),
                    userId,
                    operatorName
                );
                
                // 如果被拒绝的申请是当前已接受的申请，则清空 helpInfo 的 acceptedApplicationId 并将状态设为 OPEN
                if (applicationId.equals(helpInfo.getAcceptedApplicationId())) {
                    helpInfo.setAcceptedApplicationId(null);
                    helpInfo.setStatus("OPEN"); // 确保互助任务重新开放
                    helpInfoService.updateById(helpInfo);
                }
            }
        }

        return R.ok("申请状态更新成功", application);
    }

    /**
     * 获取特定互助任务的申请列表
     */
    @Operation(summary = "获取互助任务的申请列表")
    @GetMapping
    public R<List<HelpApplication>> getApplicationsByHelpInfoId(
            @RequestParam Long infoId,
            HttpServletRequest request) {

        // 获取当前用户ID
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.unauthorized();
        }

        // 获取互助任务
        HelpInfo helpInfo = helpInfoService.getById(infoId);
        if (helpInfo == null) {
            return R.fail("互助任务不存在");
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

    /**
     * 获取单个申请的详细信息
     */
    @Operation(summary = "获取单个申请详情")
    @GetMapping("/{applicationId}")
    public R<HelpApplication> getApplicationById(
            @PathVariable Long applicationId,
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

        // 检查权限：只有申请人或发布者可以查看详情
        if (!userId.equals(application.getApplicantId()) && !userId.equals(application.getPublisherId())) {
            return R.fail("您无权查看此申请详情");
        }

        return R.ok("获取成功", application);
    }
}
