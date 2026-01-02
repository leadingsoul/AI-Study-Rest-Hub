package com.ai_study_rest_hub_server.service;

import com.ai_study_rest_hub_server.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author leadingsoul
* @description 针对表【categories】的数据库操作Service
* @createDate 2026-01-01 17:11:12
*/
public interface CategoryService extends IService<Category> {

    /**
     * 获取所有分类信息的方法
     *
     * @return 返回一个包含所有分类信息的Category对象列表
     */
    List<Category> getAllCategories();

    /**
     * 获取分类树形结构
     * @return 返回一个包含所有分类的树形结构列表，其中每个分类节点可能包含子分类
     */
    List<Category> getCategoryTree();

    /**
     * 添加分类的方法
     * @param category 要添加的分类对象
     */
    void addCategory(Category category);  // 声明一个添加分类的方法，接收一个Category类型的参数

    /**
     * 更新分类信息的方法
     * 该方法用于接收一个Category对象，并更新对应的分类数据
     *
     * @param category 包含需要更新的分类信息的Category对象
     */
    void updateCategory(Category category);

    /**
     * 删除分类的方法
     * @param id 要删除的分类的ID
     */
    void deleteCategory(Long id);
}
