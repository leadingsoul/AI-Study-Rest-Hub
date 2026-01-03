package com.ai_study_rest_hub_server.service;

import com.ai_study_rest_hub_server.entity.Question;
import com.ai_study_rest_hub_server.vo.QuestionQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author leadingsoul
* @description 针对表【questions】的数据库操作Service
* @createDate 2026-01-01 17:14:13
*/
public interface QuestionService extends IService<Question> {

    /**
     * 自定义Java服务分页查询方法
     * 用于根据查询条件和分页参数获取问题列表
     *
     * @param pageBean 分页对象，包含分页信息和查询结果
     * @param questionQueryVo 查询条件对象，封装了查询参数
     */
    void customPageJavaService(Page<Question> pageBean, QuestionQueryVo questionQueryVo);

    /**
     * 根据问题ID查询问题信息
     * @param id 问题的唯一标识符
     * @return 返回对应ID的问题对象，如果未找到则返回null
     */
    Question queryQuestionById(Long id);

    /**
     * 保存问题信息的方法
     * @param question 要保存的问题对象，包含问题的所有相关信息
     */
    void saveQuestion(Question question);

    /**
     * 更新问题信息的方法
     * 该方法用于更新已有问题的内容，可能包括问题标题、描述、选项等信息
     *
     * @param question 包含更新后问题信息的Question对象
     */
    void updateQuestion(Question question);

    /**
     * 删除问题的方法
     * @param id 要删除的问题的ID
     */
    void deleteQuestion(Long id);

    /**
     * 获取热门问题列表
     * @param size 需要获取的问题数量
     * @return 返回包含热门问题的列表，列表长度由size参数决定
     */
    List<Question> getPopularQuestions(Integer size);

    /**
     * 根据分类ID获取问题列表
     *
     * @param categoryId 分类ID，用于筛选特定分类下的问题
     * @return 返回一个Question对象列表，包含属于该分类的所有问题
     */
    List<Question> getQuestionsByCategoryId(Long categoryId);

    /**
     * 根据难度等级获取问题列表
     * @param difficulty 问题难度等级，例如"简单"、"中等"、"困难"等
     * @return 返回符合指定难度等级的问题列表，如果没有匹配的问题则返回空列表
     */
    List<Question> getQuestionsByDifficulty(String difficulty);
}
