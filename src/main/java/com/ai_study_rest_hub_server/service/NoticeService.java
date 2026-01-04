package com.ai_study_rest_hub_server.service;

import com.ai_study_rest_hub_server.entity.Notice;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author leadingsoul
* @description 针对表【notices(公告表)】的数据库操作Service
* @createDate 2026-01-01 17:12:55
*/
public interface NoticeService extends IService<Notice> {

    /**
     * 获取所有激活状态的公告列表
     *
     * @return 返回一个包含所有激活状态公告的List集合
     */
    List<Notice> getActiveNotices();
    /**
     * 获取所有最新limit条的公告列表
     * @param limit 限制条数
     * @return 返回一个包含所有limit条的List集合
     */
    List<Notice> getLatestNotices(int limit);

    /**
     * 获取所有的公告列表
     *
     * @return 返回一个包含所有的List集合
     */
    List<Notice> getAllNotices();

    String addNotice(Notice notice);

    String updateNotice(Notice notice);

    String deleteNotice(Long id);

    String toggleNoticeStatus(Long id, Boolean isActive);
}
