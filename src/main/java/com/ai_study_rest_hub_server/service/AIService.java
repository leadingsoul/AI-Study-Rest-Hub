package com.ai_study_rest_hub_server.service;

import java.util.List;
import java.util.Map;

/**
 * AI服务接口
 * */
public interface AIService {
    /**
     * 聊天方法
     * @param topicId 话题ID，用于标识当前聊天的话题
     * @param message 用户发送的消息内容
     * @param topCategoryCode 顶级分类代码，用于标识聊天内容所属的分类
     * @param target 聊天目标，可能是用户ID或其他标识符
     * @param chatHistory 聊天历史记录，包含之前的对话内容
     * @return 返回一个Map<String, Object>对象，包含聊天响应的相关信息
     */
    Map<String, Object> chat(Long topicId, String message, String topCategoryCode, String target, List<Map<String, String>> chatHistory);
}
