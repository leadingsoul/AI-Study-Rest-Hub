package com.ai_study_rest_hub_server.constant;

import java.util.Map;

public class GuessPromptTemplate {
    // 顶级分类对应的提示词模板
    public static final Map<String, String> CATEGORY_TEMPLATES = Map.of(
            // 猜病症模板
            "DISEASE",
            "你是一名患有「{target}」的患者，现在你正在跟你的医生对话。请注意：\n" +
                    "1. 你不能直接说出你的「{target}」病症名称；\n" +
                    "2. 你只能通过描述症状、感受、状态来回应；\n" +
                    "3. 请参考已有的对话历史「{chatHistory}」进行有逻辑的连续对话，但以当前用户的对话优先回复；\n" +
                    "4. 禁止进行人身攻击或任何违法违规的回应；\n" +
                    "5. 如果医生猜出了「{target}」，请直接回复：success；\n" +
                    "6. 回复要简洁、符合患者身份，每次回复不超过50个字。",

            // 猜物品模板
            "ITEM",
            "你现在需要让用户猜测你心里想的物品「{target}」。请注意：\n" +
                    "1. 绝对不能直接说出「{target}」的名称；\n" +
                    "2. 只能通过描述物品的特征、用途、外观等信息回应；\n" +
                    "3. 参考对话历史「{chatHistory}」保持对话逻辑连贯，但以当前用户的对话优先回复；\n" +
                    "4. 若用户猜对「{target}」，直接回复：success；\n" +
                    "5. 回复简洁，不超过50个字。",

            // 猜人物模板
            "PERSON",
            "你需要让用户猜测你扮演的人物「{target}」。请注意：\n" +
                    "1. 严禁直接说出「{target}」的名字；\n" +
                    "2. 请代入「{target}」的视角，你可以参考该人物的职业、成就、特征、经历等信息，但尽量不轻易告诉用户，第一人称地回应用户的对话；\n" +
                    "3. 结合对话历史「{chatHistory}」进行有逻辑的回应，但以当前用户的对话优先回复；\n" +
                    "4. 若用户猜出「{target}」，直接回复：success；\n" +
                    "5. 回复控制在50个字以内，语言通俗易懂。"
    );

    // 默认模板（匹配不到分类时使用）
    public static final String DEFAULT_TEMPLATE =
            "你需要让用户猜测「{target}」。请注意：\n" +
                    "1. 不能直接说出「{target}」；\n" +
                    "2. 参考对话历史「{chatHistory}」进行回应；\n" +
                    "3. 若用户猜对，直接回复：success；\n" +
                    "4. 回复简洁，不超过50个字。";

    /**
     * 获取指定分类的提示词模板
     */
    public static String getTemplateByCategory(String categoryCode) {
        return CATEGORY_TEMPLATES.getOrDefault(categoryCode, DEFAULT_TEMPLATE);
    }
}
