package com.example.campusbuddy.controller;

import com.example.campusbuddy.entity.Review;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.service.ReviewService;
import com.example.campusbuddy.service.UserService;
import com.example.campusbuddy.vo.ReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Tag(name = "评价接口", description = "评价相关操作")
@RestController
@RequestMapping("/api/review")
public class ReviewController {
    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Operation(summary = "提交评价")
    @PostMapping("/submit")
    public ResponseEntity<?> submitReview(@RequestBody Review review) {
        log.info("收到评价提交请求: {}", review);

        // 验证必要字段
        if (review.getReviewedUserId() == null) {
            log.error("评价提交失败: 缺少被评价用户ID");
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "缺少被评价用户ID"));
        }

        if (review.getReviewerUserId() == null) {
            log.error("评价提交失败: 缺少评价者用户ID");
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "缺少评价者用户ID"));
        }

        if (review.getRelatedInfoId() == null) {
            log.error("评价提交失败: 缺少关联互助信息ID");
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "缺少关联互助信息ID"));
        }

        boolean result = reviewService.submitReview(review);
        if (result) {
            return ResponseEntity.ok(Map.of("success", true));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "评价提交失败"));
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        log.error("处理评价请求时发生异常", e);
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "服务器处理请求时发生错误: " + e.getMessage()));
    }

    @Operation(summary = "查询某个互助的评价")
    @GetMapping("/byHelp/{helpId}")
    public List<ReviewVO> getReviewsByHelpId(@PathVariable Long helpId) {
        return reviewService.getReviewsByHelpId(helpId);
    }

    @Operation(summary = "查询某个用户收到的评价")
    @GetMapping("/byUser/{userId}")
    public List<ReviewVO> getReviewsByUserId(@PathVariable Long userId) {
        return reviewService.getReviewsByUserId(userId);
    }

    @Operation(summary = "获取用户的信用分")
    @GetMapping("/credit/{userId}")
    public Integer getUserCreditScore(@PathVariable Long userId) {
        return reviewService.getUserCreditScore(userId);
    }

    @Operation(summary = "检查用户是否已对某互助进行评价")
    @GetMapping("/check")
    public boolean hasReviewed(@RequestParam Long reviewerId, @RequestParam Long helpInfoId) {
        return reviewService.hasReviewed(reviewerId, helpInfoId);
    }

    @Operation(summary = "检查用户是否可以对互助信息进行评价")
    @GetMapping("/canReview")
    public boolean canReview(@RequestParam Long userId,
            @RequestParam Long helpInfoId,
            @RequestParam String reviewType) {
        return reviewService.canReview(userId, helpInfoId, reviewType);
    }

    @Operation(summary = "获取互助信息的评价状态")
    @GetMapping("/status")
    public Map<String, Boolean> getHelpInfoReviewStatus(@RequestParam Long helpInfoId) {
        return reviewService.getHelpInfoReviewStatus(helpInfoId);
    }

    @Operation(summary = "分页&多条件查询评价列表")
    @GetMapping("/list")
    public com.example.campusbuddy.vo.PageResult<ReviewVO> getReviewList(
            @RequestParam Long userId,
            @RequestParam(required = false) String type, // received/given
            @RequestParam(required = false) Integer score,
            @RequestParam(required = false) String moduleType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return reviewService.getReviewList(userId, type, score, moduleType, page, size);
    }

    @Operation(summary = "获取用户对特定互助信息的评价状态")
    @GetMapping("/userReviewStatus")
    public ResponseEntity<?> getUserReviewStatus(
            @RequestParam Long userId,
            @RequestParam Long helpInfoId) {
        log.info("获取用户评价状态: userId={}, helpInfoId={}", userId, helpInfoId);
        try {
            Map<String, Object> result = reviewService.getUserReviewStatus(userId, helpInfoId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取用户评价状态失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "获取用户评价状态失败: " + e.getMessage()));
        }
    }
}
