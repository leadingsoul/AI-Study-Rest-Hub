package com.ai_study_rest_hub_server.mapper;

import com.ai_study_rest_hub_server.entity.GuessTopic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
* @author leadingsoul
* @description 针对表【guess_topics( 猜词题目表 )】的数据库操作Mapper
* @createDate 2026-01-01 17:12:44
* @Entity com.ai_study_rest_hub_server.entity.GuessTopics
*/
public interface GuessTopicMapper extends BaseMapper<GuessTopic> {

    IPage<GuessTopic> getTopicList(Page<GuessTopic> pageObj, Long categoryId, String difficulty, String keyword);
}




