package com.ai_study_rest_hub_server.service.impl;

import com.ai_study_rest_hub_server.entity.Question;
import com.ai_study_rest_hub_server.mapper.QuestionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ai_study_rest_hub_server.entity.Category;
import com.ai_study_rest_hub_server.service.CategoryService;
import com.ai_study_rest_hub_server.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author 刘博啸辉
* @description 针对表【categories】的数据库操作Service实现
* @createDate 2026-01-01 17:11:12
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final QuestionMapper questionMapper;

    @Override
    public List<Category> getAllCategories() {
        //获取所有分类基本信息
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .orderByAsc(Category::getSort)
        );
        //填充题目数量
        fillQuestionCount(categories);
        return categories;
    }

    @Override
    public List<Category> getCategoryTree() {
        // 1. 获取所有分类，并按sort排序
        List<Category> allCategories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .orderByAsc(Category::getSort)
        );

        // 2. 为每个分类填充其自身的题目数量
        fillQuestionCount(allCategories);
        // 3. 构建树形结构并返回
        List<Category> buildTree = buildTree(allCategories);
        log.info("查询类别树状结构集合：{}",buildTree);
        return buildTree;
    }

    @Override
    public void addCategory(Category category) {
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Category::getParentId,category.getParentId());
        lambdaQueryWrapper.eq(Category::getName,category.getName());
        long count = count(lambdaQueryWrapper);
        if(count>0){
            Category parent = getById(category.getParentId());
            throw new RuntimeException("分类名称已存在，请重新输入！父分类名称为："+parent.getName());
        }
        save(category);
    }

    @Override
    public void updateCategory(Category category) {
        //先校验，同一父分类下 可以跟自己的name重复，不能跟其他子分类重复
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Category::getParentId,category.getParentId());
        lambdaQueryWrapper.eq(Category::getName,category.getName());
        lambdaQueryWrapper.eq(Category::getId,category.getId());
        CategoryMapper categoryMapper = getBaseMapper();
        boolean exists = categoryMapper.exists(lambdaQueryWrapper);
        if(exists){
            Category parent = getById(category.getParentId());
            throw new RuntimeException("分类名称已存在，请重新输入！父分类名称为："+parent.getName());
        }
        updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        //1.检查是否是一级标题
        Category category = getById(id);
        if(category.getParentId()==0){
            throw new RuntimeException("一级标题不能删除");
        }
        //2.检查是否有题目关联
        LambdaQueryWrapper<Question> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Question::getCategoryId,id);
        long count = questionMapper.selectCount(lambdaQueryWrapper);
        if(count>0){
            throw new RuntimeException("该分类下有题目，不能删除");
        }
        removeById(id);
    }

    private List<Category> buildTree(List<Category> allCategories) {
        //1.使用stream流按parentId进行分组，得到Map<parentId, List<children>>
        Map<Long, List<Category>> categoryMap = allCategories.stream().collect(Collectors.groupingBy(Category::getParentId));
        //2.遍历所有分类，将每个分类的子分类填充到children中
        allCategories.forEach(category -> {
            List<Category> children = categoryMap.getOrDefault(category.getId(), new ArrayList<>());
            category.setChildren(children);
            long childrenQuestionCount = children.stream()
                    .mapToLong(child -> child.getCount()!=null?child.getCount():0L)
                    .sum();
            long selfQuestionCount = category.getCount()!=null?category.getCount():0L;
            category.setCount(childrenQuestionCount + selfQuestionCount);
        });
        //3.最后，筛选出所有顶级分类(parentId=0)
        return allCategories.stream()
                .filter(category -> category.getParentId() == 0)
                .collect(Collectors.toList());
    }

    private void fillQuestionCount(List<Category> categories) {
        List<Map<Long,Object>> questionCountList = questionMapper.getCategoryQuestionCount();

        Map<Long,Long> questionCountMap = questionCountList.stream().collect(Collectors.toMap(
                map -> Long.valueOf(map.get("category_id").toString()),
                map -> Long.valueOf(map.get("count").toString())
        ));
        categories.forEach(category -> {
            category.setCount(questionCountMap.getOrDefault(category.getId(),0L));
        });
    }
}




