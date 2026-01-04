package com.ai_study_rest_hub_server.mapper;

import com.ai_study_rest_hub_server.entity.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
* @author leadingsoul
* @description 针对表【questions】的数据库操作Mapper
* @createDate 2026-01-01 17:14:13
* @Entity com.ai_study_rest_hub_server.entity.Questions
*/
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 查询每个分类下的题目数量
     * 统计非删除状态(is_deleted=0)的题目按分类(category_id)分组的数量
     *
     * @return 返回一个Map列表，每个Map包含分类ID(category_id)和对应的题目数量(count)
     *         Map的键为Long类型(分类ID)，值为Object类型(题目数量)
     */
    @Select("SELECT category_id, COUNT(*) as count FROM questions where is_deleted = 0  GROUP BY category_id ; ")
    List<Map<Long, Object>> getCategoryQuestionCount();

    /**
     * 根据页面ID查询问题列表
     * 该方法用于从数据库中获取指定页面ID对应的所有问题记录
     *
     * @param id 页面ID，用于筛选特定页面的问题
     * @return List<Question> 返回问题对象列表，包含该页面下的所有问题信息
     *         如果未找到任何问题，可能返回空列表而非null
     */
    List<Question> queryQuestionListByPageId(Integer id);
}




