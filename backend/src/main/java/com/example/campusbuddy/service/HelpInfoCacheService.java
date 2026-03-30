package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusbuddy.entity.HelpInfo;
import com.example.campusbuddy.vo.HelpInfoDetailVO;

public interface HelpInfoCacheService {

    void cacheHelpInfoList(String cacheKey, Page<HelpInfo> helpInfoPage, long expireSeconds);

    Page<HelpInfo> getCachedHelpInfoList(String cacheKey);

    void cacheHelpInfoDetail(Long helpInfoId, HelpInfoDetailVO helpInfoDetail, long expireSeconds);

    HelpInfoDetailVO getCachedHelpInfoDetail(Long helpInfoId);

    void clearHelpInfoCache(Long helpInfoId);

    void clearHelpInfoListCache();

    void clearAllHelpInfoCache();

    String generateListCacheKey(long page, long size, String type, String status,
                               String publisherId, String keyword);
}
