package com.ai_study_rest_hub_server.service.impl;

import com.ai_study_rest_hub_server.entity.GuessTopic;
import com.ai_study_rest_hub_server.mapper.GuessTopicMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ai_study_rest_hub_server.entity.GuessCategory;
import com.ai_study_rest_hub_server.service.GuessCategorieService;
import com.ai_study_rest_hub_server.mapper.GuessCategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author 刘博啸辉
* @description 针对表【guess_categories( 猜词分类表 )】的数据库操作Service实现
* @createDate 2026-01-01 17:12:19
*/
@Slf4j
@RequiredArgsConstructor
@Service
public class GuessCategorieServiceImpl extends ServiceImpl<GuessCategoryMapper, GuessCategory>
    implements GuessCategorieService {

    private final GuessCategoryMapper guessCategoryMapper;
    private final GuessTopicMapper guessTopicMapper;

    /**
     * 获取所有启用的分类并构建树形结构
     * @return 返回构建好的分类树形结构列表
     */
    @Override
    public List<GuessCategory> getGuessCategories() {
        // 获取所有启用的分类
        // 使用LambdaQueryWrapper构建查询条件
        // 1. 只查询状态为1（启用）的分类
        // 2. 按照sortOrder字段升序排序
        List<GuessCategory> allCategories = guessCategoryMapper.selectList(
                new LambdaQueryWrapper<GuessCategory>()
                        .eq(GuessCategory::getStatus, 1)
                        .orderByAsc(GuessCategory::getSortOrder)
        );
        // 调用buildTree方法将平铺的分类列表转换为树形结构
        return buildTree(allCategories);
    }

    /**
     * 保存猜谜分类的方法
     * 这是一个接口方法的实现，用于将猜谜分类信息保存到数据存储中
     *
     * @param guessCategory 包含猜谜分类信息的对象，需要被保存
     */
    @Override
    public void saveCategory(GuessCategory guessCategory) {
        if(guessCategory.getParentId() != null&& guessCategory.getParentId() > 0){
            GuessCategory parentCategory = guessCategoryMapper.selectById(guessCategory.getParentId());
            if(parentCategory == null){
                throw new RuntimeException("父级分类不存在");
            }
            if(parentCategory.getStatus() == 0) {
                throw new RuntimeException("父级分类已被禁用");
            }
        }
        // 检查同级分类名称是否重复
        Long count = guessCategoryMapper.selectCount(
                new LambdaQueryWrapper<GuessCategory>()
                        .eq(GuessCategory::getName, guessCategory.getName())
                        .eq(GuessCategory::getParentId, guessCategory.getParentId() == null ? 0 :guessCategory.getParentId())

        );
        if (count > 0) {
            throw new RuntimeException("同级分类下已存在相同名称的分类");
        }
        // 设置默认值
        if (guessCategory.getParentId() == null) {
            guessCategory.setParentId(0L);
        }
        if (guessCategory.getSortOrder() == null) {
            guessCategory.setSortOrder(0);
        }
        if (guessCategory.getStatus() == null) {
            guessCategory.setStatus(1);
        }
        guessCategoryMapper.insert(guessCategory);
    }

    @Override
    public void updateCategory(Long id, GuessCategory guessCategory) {
        GuessCategory existingCategory = guessCategoryMapper.selectById(id);
        if (existingCategory == null) {
            throw new RuntimeException("分类不存在");
        }
        //验证父级
        if(guessCategory.getParentId() != null&& guessCategory.getParentId() > 0){
            if(guessCategory.getParentId().equals(id)){
                throw new RuntimeException("自己不能分类成父级");
            }
            //验证父级存在性
            GuessCategory parentCategory = guessCategoryMapper.selectById(guessCategory.getParentId());
            if(parentCategory == null){
                throw new RuntimeException("父级分类不存在");
            }
            //验证父级状态
            if(parentCategory.getStatus() == 0) {
                throw new RuntimeException("父级分类已被禁用");
            }
            // 检查同级分类名称是否重复
            Long count = guessCategoryMapper.selectCount(
                new LambdaQueryWrapper<GuessCategory>()
                        .eq(GuessCategory::getName, guessCategory.getName())
                        .eq(GuessCategory::getParentId, guessCategory.getParentId() == null ? 0 :guessCategory.getParentId())
                        .ne(GuessCategory::getId, id)
                );
            if(count > 0) {
                throw new RuntimeException("同级分类下已存在相同名称的分类");
            }
            guessCategoryMapper.updateById(guessCategory);
        }
    }

    @Override
    public void deleteCategory(Long id) {
        GuessCategory category = guessCategoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        Long ChildrenCount = guessCategoryMapper.selectCount(
                new LambdaQueryWrapper<GuessCategory>()
                        .eq(GuessCategory::getParentId, id)

        );
        if (ChildrenCount > 0) {
            throw new RuntimeException("该分类下存在子分类，无法删除");
        }
        Long topicCount = guessTopicMapper.selectCount(
                new LambdaQueryWrapper<GuessTopic>()
                        .eq(GuessTopic::getCategoryId, id));
        if (topicCount > 0) {
            throw new RuntimeException("该分类下存在题目，无法删除");
        }
        guessCategoryMapper.deleteById(id);
    }


    /**
     * 构建树形结构
     */
        private List<GuessCategory> buildTree(List<GuessCategory> categories) {
        // 按parentId分组
        Map<Long, List<GuessCategory>> childrenMap = categories.stream()
                .collect(Collectors.groupingBy(GuessCategory::getParentId));

        // 设置children属性，并从下至上汇总题目数量
        categories.forEach(category -> {
            List<GuessCategory> children = childrenMap.getOrDefault(category.getId(), new ArrayList<>());
            category.setChildren(children);
        });

        // 返回顶级分类（parentId = 0）
        return categories.stream()
                .filter(c -> c.getParentId() == 0)
                .collect(Collectors.toList());
    }
}




