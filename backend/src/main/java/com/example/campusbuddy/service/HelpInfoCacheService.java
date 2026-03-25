package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusbuddy.entity.HelpInfo;
import com.example.campusbuddy.vo.HelpInfoDetailVO;
import com.example.campusbuddy.vo.HelpInfoVO;

public interface HelpInfoCacheService {

    void cacheHelpInfoList(String cacheKey, Page<HelpInfo> helpInfoPage, long expireSeconds);

    Page<HelpInfo> getCachedHelpInfoList(String cacheKey);

    void cacheHelpInfoDetail(Long helpInfoId, HelpInfoDetailVO helpInfoDetail, long expireSeconds);

    HelpInfoDetailVO getCachedHelpInfoDetail(Long helpInfoId);

    void cacheAdminHelpInfoList(String cacheKey, Page<HelpInfoVO> helpInfoPage, long expireSeconds);

    Page<HelpInfoVO> getCachedAdminHelpInfoList(String cacheKey);

    void cacheSearchResults(String searchKey, Page<HelpInfo> helpInfoPage, long expireSeconds);

    Page<HelpInfo> getCachedSearchResults(String searchKey);

    void clearHelpInfoCache(Long helpInfoId);

    void clearHelpInfoListCache();

    void clearAllHelpInfoCache();

    String generateListCacheKey(long page, long size, String type, String status,
                               String publisherId, String keyword);

    String generateSearchCacheKey(String keyword, String type, String status,
                                 long page, long size);

    String generateAdminCacheKey(Integer page, Integer size, String keyword,
                                String type, String status);
}
