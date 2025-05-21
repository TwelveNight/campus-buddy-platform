package com.example.campusbuddy.controller;

import com.example.campusbuddy.common.R;
import com.example.campusbuddy.entity.HelpApplication;
import com.example.campusbuddy.service.HelpApplicationService;
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

    @Operation(summary = "提交互助申请")
    @PostMapping
    public R<HelpApplication> apply(@RequestBody HelpApplication application, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null)
            return R.unauthorized();
        application.setApplicantId(userId);
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

    // 可继续补充：接受/拒绝/撤销申请、按状态筛选等接口
}
