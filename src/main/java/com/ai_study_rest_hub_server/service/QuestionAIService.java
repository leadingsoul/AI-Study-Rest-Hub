package com.ai_study_rest_hub_server.service;

import com.ai_study_rest_hub_server.vo.AiGenerateRequestVo;
import com.ai_study_rest_hub_server.vo.QuestionImportVo;

import java.io.InterruptedIOException;
import java.util.List;

/**
 * 用于AI生成题目
 * */
public interface QuestionAIService {
    /**
     * 构建AI生成提示词的方法
     * 该方法用于根据请求参数构建一个提示词字符串，供AI模型使用
     *
     * @param request AI生成请求的值对象，包含构建提示词所需的参数
     * @return 返回构建好的提示词字符串
     */
    public String buildPrompt(AiGenerateRequestVo request);

    /**
     * 根据AI生成请求生成问题列表
     *
     * @param request AI生成请求参数，包含生成问题所需的各项配置信息
     * @return 返回生成的问题列表，每个问题以QuestionImportVo对象形式封装
     */
    List<QuestionImportVo> aiGenerateQuestions(AiGenerateRequestVo request) throws InterruptedIOException;
}
