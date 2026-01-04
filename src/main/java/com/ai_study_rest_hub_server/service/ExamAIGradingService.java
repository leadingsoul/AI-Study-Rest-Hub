package com.ai_study_rest_hub_server.service;

import com.ai_study_rest_hub_server.entity.Question;
import com.ai_study_rest_hub_server.vo.GradingResult;

import java.io.InterruptedIOException;

/**
 * AI判卷服务
 * 使用AI进行智能批改和点评
 * */
public interface ExamAIGradingService {

    /**
     * 构建评分提示信息
     * @param question 问题对象，包含题目内容和评分标准等信息
     * @param userAnswer 用户的答案内容
     * @param maxScore 该题目的最高分值
     * @return 返回格式化的评分提示信息，用于指导评分过程
     */
    public String buildGradingPrompt(Question question, String userAnswer, Integer maxScore);

    /**
     * 构建总结提示信息的公共方法
     * @param totalScore 总得分
     * @param maxScore 最大可能得分
     * @param questionCount 总题目数量
     * @param correctCount 正确回答的题目数量
     * @return 返回格式化后的总结提示字符串
     */
    public String buildSummaryPrompt(Integer totalScore, Integer maxScore, Integer questionCount, Integer correctCount);

    /**
     * 评分文本题目的方法
     * @param question 问题对象，包含题目相关信息
     * @param userAnswer 用户提交的答案
     * @param maxScore 题目序号或相关参数
     * @return 返回一个GradingResult对象，包含评分结果和相关信息
     */
    GradingResult gradingTextQuestion(Question question, String userAnswer, Integer maxScore) throws InterruptedIOException;

/**
 * 构建一个总结字符串的方法
 * @param totalScore 总得分
 * @param maxScore 当前索引或序号
 * @param questionCount 问题总数（使用Integer包装类）
 * @param correctCount 正确回答的数量
 * @return 返回一个包含总结信息的字符串
 */
    String buildSummary(Integer totalScore, Integer maxScore, Integer questionCount, Integer correctCount) throws InterruptedIOException;
}
