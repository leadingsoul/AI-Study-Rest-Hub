package com.ai_study_rest_hub_server.service.impl;

import com.ai_study_rest_hub_server.entity.GuessCategory;
import com.ai_study_rest_hub_server.mapper.GuessCategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ai_study_rest_hub_server.entity.GuessTopic;
import com.ai_study_rest_hub_server.service.GuessTopicService;
import com.ai_study_rest_hub_server.mapper.GuessTopicMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
* @author 刘博啸辉
* @description 针对表【guess_topics( 猜词题目表 )】的数据库操作Service实现
* @createDate 2026-01-01 17:12:44
*/
@Slf4j
@RequiredArgsConstructor
@Service
public class GuessTopicServiceImpl extends ServiceImpl<GuessTopicMapper, GuessTopic>
    implements GuessTopicService {

    private final GuessTopicMapper guessTopicMapper;
    private final GuessCategoryMapper guessCategoryMapper;

    @Override
    public IPage<GuessTopic> getTopicList(Integer page, Integer size, Long categoryId, String difficulty, String keyword) {
        Page<GuessTopic> pageObj= new Page<>(page, size);
        IPage<GuessTopic> result = guessTopicMapper.getTopicList(pageObj, categoryId, difficulty, keyword);
        return result;
    }

    @Override
    public void saveTopic(GuessTopic guessTopic) {
        if(guessTopic.getCategoryId()!=null&&guessTopic.getCategoryId()>0){
            GuessCategory category = guessCategoryMapper.selectById(guessTopic.getCategoryId());
            if(category==null){
                throw new RuntimeException("分类不存在");
            }
            if(category.getStatus()==0) { // 分类状态为禁用
                throw new RuntimeException("分类已禁用");
            }
        }
        //检查是否重复target
        Long count = guessTopicMapper.selectCount(new QueryWrapper<GuessTopic>().eq("target", guessTopic.getTarget()));
        if(count>0){
            throw new RuntimeException("目标词已存在");
        }
        guessTopic.setRecordCount(0L);
        guessTopicMapper.insert(guessTopic);
    }

    @Override
    public void updateTopic(Long id, GuessTopic guessTopic) {
        GuessTopic existingTopic = guessTopicMapper.selectById(id);
        if (existingTopic == null) {
            throw new RuntimeException("题目不存在");
        }
        Long count = guessTopicMapper.selectCount(new QueryWrapper<GuessTopic>().eq("target", guessTopic.getTarget()).ne("id", id));
        if(count>0){
            throw new RuntimeException("目标词已存在");
        }
        guessTopicMapper.updateById(guessTopic);
    }

    @Override
    public void deleteTopic(Long id) {
        GuessTopic topic = guessTopicMapper.selectById(id);
        if(topic==null){
            throw new RuntimeException("题目不存在");
        }
        guessTopicMapper.deleteById(id);
    }
}




