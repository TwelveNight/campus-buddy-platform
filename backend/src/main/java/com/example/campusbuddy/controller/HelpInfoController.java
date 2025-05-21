package com.example.campusbuddy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusbuddy.common.R;
import com.example.campusbuddy.entity.HelpInfo;
import com.example.campusbuddy.service.HelpInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/helpinfo")
@Tag(name = "HelpInfo", description = "互助信息相关接口")
public class HelpInfoController {
    @Autowired
    private HelpInfoService helpInfoService;

    @PostMapping
    @Operation(summary = "发布互助信息")
    public R<HelpInfo> create(@RequestBody HelpInfo helpInfo) {
        helpInfoService.save(helpInfo);
        return R.ok(helpInfo);
    }

    @GetMapping
    @Operation(summary = "分页查询互助信息列表")
    public R<IPage<HelpInfo>> list(@RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        QueryWrapper<HelpInfo> wrapper = new QueryWrapper<>();
        if (type != null)
            wrapper.eq("type", type);
        if (status != null)
            wrapper.eq("status", status);
        IPage<HelpInfo> result = helpInfoService.page(new Page<>(page, size), wrapper);
        return R.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查看互助信息详情")
    public R<HelpInfo> detail(@PathVariable Long id) {
        HelpInfo info = helpInfoService.getById(id);
        if (info == null)
            return R.fail(404, "互助信息不存在");
        return R.ok(info);
    }
}
