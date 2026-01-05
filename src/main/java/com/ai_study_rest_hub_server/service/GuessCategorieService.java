package com.ai_study_rest_hub_server.service;

import com.ai_study_rest_hub_server.entity.GuessCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author leadingsoul
* @description 针对表【guess_categories( 猜词分类表 )】的数据库操作Service
* @createDate 2026-01-01 17:12:19
*/
public interface GuessCategorieService extends IService<GuessCategory> {

    /**
     * 获取分类列表
     * @return 分类列表数据
     */
    List<GuessCategory> getGuessCategories();

    /**
     * 保存分类
     * @param guessCategory 分类对象
     * */
    void saveCategory(GuessCategory guessCategory);

    /**
     * 更新分类
     * @param guessCategory 分类对象
     * @param id 分类ID
     * */
    void updateCategory(Long id, GuessCategory guessCategory);

    /**
     * 删除分类
     * @param id 分类ID
     * */
    void deleteCategory(Long id);
}
