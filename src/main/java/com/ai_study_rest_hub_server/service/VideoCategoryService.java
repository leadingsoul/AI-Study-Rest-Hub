package com.ai_study_rest_hub_server.service;

import com.ai_study_rest_hub_server.entity.VideoCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author leadingsoul
* @description 针对表【video_categories(视频分类表)】的数据库操作Service
* @createDate 2026-01-01 17:14:28
*/
public interface VideoCategoryService extends IService<VideoCategory> {

    /**
     * 获取所有视频分类的方法
     *
     * @return 返回包含所有视频分类的列表
     *         VideoCategory 是视频分类的数据类型
     */
    List<VideoCategory> getAllCategories();

    /**
     * 获取视频分类树形结构
     *
     * @return 返回视频分类树形结构的列表，包含所有层级的分类信息
     */
    List<VideoCategory> getCategoryTree();

    /**
     * 获取顶级视频分类列表的方法
     * 该方法用于从系统中获取所有顶级分类的集合
     *
     * @return 返回一个包含顶级视频分类对象的列表
     *         每个VideoCategory对象代表一个视频分类
     *         列表可能为空，但不会返回null
     */
    List<VideoCategory> getTopCategories();

    /**
     * 根据父分类ID获取所有子分类列表
     *
     * @param parentId 父分类ID，用于查询其下的所有子分类
     * @return 返回VideoCategory类型的列表，包含所有符合条件的子分类信息
     */
    List<VideoCategory> getChildCategories(Long parentId);

    /**
     * 根据ID获取视频分类信息
     * @param id 视频分类的唯一标识符
     * @return 返回对应的视频分类对象，如果未找到则返回null
     */
    VideoCategory getCategoryById(Long id);

    /**
     * 添加视频分类的方法
     * @param category 要添加的视频分类对象
     */
    void addCategory(VideoCategory category);

    /**
     * 更新视频分类信息的方法
     * @param category 包含更新后视频分类信息的对象
     */
    void updateCategory(VideoCategory category);

    /**
     * 删除分类的方法
     * @param id 要删除的分类ID
     */
    void deleteCategory(Long id);
}
