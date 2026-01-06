package com.ai_study_rest_hub_server.service.impl;

import com.ai_study_rest_hub_server.entity.Category;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public IPage<GuessTopic> getTopicList(Integer page, Integer size, Long categoryId,String topCategoryCode ,String difficulty, String keyword) {
        Page<GuessTopic> pageObj= new Page<>(page, size);
        IPage<GuessTopic> result = guessTopicMapper.getTopicList(pageObj, categoryId, topCategoryCode,difficulty, keyword);
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

    @Override
    public GuessTopic getRandomTopic(Long categoryId) {
        // 1. 获取当前分类ID及其所有子分类ID列表
        List<Long> categoryIds = getAllCategoryIdsWithChildren(categoryId);
        // 校验分类ID列表是否为空
        if (categoryIds == null || categoryIds.isEmpty()) {
            return null;
        }
        // 2. 查询所有指定分类（含子类）下的题目
        List<GuessTopic> topics = guessTopicMapper.selectList(
                new QueryWrapper<GuessTopic>()
                        .in("category_id", categoryIds)
        );
        if (topics == null || topics.isEmpty()) {
            return null; // 没有找到题目，返回null
        }
        Collections.shuffle(topics);
        return topics.get(0);
    }

    /**
     * 获取指定分类ID及其所有子分类的ID列表
     * @param categoryId 父分类ID
     * @return 包含父分类和所有子分类的ID列表
     */
    private List<Long> getAllCategoryIdsWithChildren(Long categoryId) {
        List<Long> categoryIds = new ArrayList<>();
        // 先添加当前分类ID
        categoryIds.add(categoryId);
        // 查询当前分类的直接子分类
        List<Long> childIds = guessCategoryMapper.selectList(
                new QueryWrapper<GuessCategory>()
                        .eq("parent_id", categoryId)
                        .select("id")
        ).stream().map(GuessCategory::getId).toList();

        // 递归添加所有子分类的ID
        for (Long childId : childIds) {
            categoryIds.addAll(getAllCategoryIdsWithChildren(childId));
        }
        return categoryIds;
    }
}




