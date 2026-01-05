package com.ai_study_rest_hub_server.service;

import com.ai_study_rest_hub_server.entity.GuessTopic;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author leadingsoul
* @description 针对表【guess_topics( 猜词题目表 )】的数据库操作Service
* @createDate 2026-01-01 17:12:44
*/
public interface GuessTopicService extends IService<GuessTopic> {

    /**
     * 分页获取猜谜主题列表的方法
     *
     * @param page 当前页码，用于分页查询
     * @param size 每页显示数量，用于分页查询
     * @param categoryId 主题分类ID，用于按分类筛选
     * @param difficulty 难度级别，用于按难度筛选
     * @param keyword 关键词，用于搜索主题标题或内容
     * @return 返回一个IPage类型的GuessTopic对象，包含分页信息和主题列表数据
     */
    IPage<GuessTopic> getTopicList(Integer page, Integer size, Long categoryId, String difficulty, String keyword);

    /**
     * 保存主题信息的方法
     * @param guessTopic 要保存的主题对象，包含主题相关的所有信息
     */
    void saveTopic(GuessTopic guessTopic);

    /**
     * 更新主题信息的方法
     * @param id 需要更新的主题ID
     * @param guessTopic 包含更新后主题信息的对象
     */
    void updateTopic(Long id, GuessTopic guessTopic);

    /**
     * 删除主题的方法
     * 根据传入的主题ID删除对应的主题信息
     *
     * @param id 主题的唯一标识符，用于定位要删除的主题
     */
    void deleteTopic(Long id);
}
