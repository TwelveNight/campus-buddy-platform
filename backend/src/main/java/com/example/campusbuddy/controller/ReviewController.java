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

@Tag(name = "评价接口", description = "评价相关操作")
@RestController
@RequestMapping("/api/review")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Operation(summary = "提交评价")
    @PostMapping("/submit")
    public boolean submitReview(@RequestBody Review review) {
        return reviewService.submitReview(review);
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
}
