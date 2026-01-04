package com.ai_study_rest_hub_server.service.impl;

import com.ai_study_rest_hub_server.entity.Question;
import com.ai_study_rest_hub_server.service.ExamAIGradingService;
import com.ai_study_rest_hub_server.vo.GradingResult;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import java.io.InterruptedIOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AI批改试卷实现类
 * */
@Slf4j
@RequiredArgsConstructor
@Service
public class ExamAIGradingServiceImpl implements ExamAIGradingService {

    private final ChatClient chatClient;

    @Override
    public String buildGradingPrompt(Question question, String userAnswer, Integer maxScore) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一名专业的考试阅卷老师，请对以下题目进行判卷：\n\n");

        prompt.append("【题目信息】\n");
        prompt.append("题型：").append(getQuestionTypeText(question.getType())).append("\n");
        prompt.append("题目：").append(question.getTitle()).append("\n");
        prompt.append("标准答案：").append(question.getAnswer().getAnswer()).append("\n");
        prompt.append("满分：").append(maxScore).append("分\n\n");

        prompt.append("【学生答案】\n");
        prompt.append(userAnswer.trim().isEmpty() ? "（未作答）" : userAnswer).append("\n\n");

        prompt.append("【判卷要求】\n");
        if ("CHOICE".equals(question.getType()) || "JUDGE".equals(question.getType())) {
            prompt.append("- 客观题：答案完全正确得满分，答案错误得0分\n");
        } else if ("TEXT".equals(question.getType())) {
            prompt.append("- 主观题：根据答案的准确性、完整性、逻辑性进行评分\n");
            prompt.append("- 答案要点正确且完整：80-100%分数\n");
            prompt.append("- 答案基本正确但不够完整：60-80%分数\n");
            prompt.append("- 答案部分正确：30-60%分数\n");
            prompt.append("- 答案完全错误或未作答：0分\n");
        }

        prompt.append("\n请按以下JSON格式返回判卷结果：\n");
        prompt.append("{\n");
        prompt.append("  \"score\": 实际得分(整数),\n");
        prompt.append("  \"feedback\": \"具体的评价反馈(50字以内)\",\n");
        prompt.append("  \"reason\": \"扣分原因或得分依据(30字以内)\"\n");
        prompt.append("}");

        return prompt.toString();
    }

    @Override
    public String buildSummaryPrompt(Integer totalScore, Integer maxScore, Integer questionCount, Integer correctCount) {
        double percentage = (double) totalScore / maxScore * 100;

        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一名资深的教育专家，请为学生的考试表现提供专业的总评和学习建议：\n\n");

        prompt.append("【考试成绩】\n");
        prompt.append("总得分：").append(totalScore).append("/").append(maxScore).append("分\n");
        prompt.append("得分率：").append(String.format("%.1f", percentage)).append("%\n");
        prompt.append("题目总数：").append(questionCount).append("道\n");
        prompt.append("答对题数：").append(correctCount).append("道\n\n");

        prompt.append("【要求】\n");
        prompt.append("请提供一份150字左右的考试总评，包括：\n");
        prompt.append("1. 对本次考试表现的客观评价\n");
        prompt.append("2. 指出优势和不足之处\n");
        prompt.append("3. 提供具体的学习建议和改进方向\n");
        prompt.append("4. 给予鼓励和激励\n\n");

        prompt.append("请直接返回总评内容，无需特殊格式：");

        return prompt.toString();
    }

    @Override
    public GradingResult gradingTextQuestion(Question question, String userAnswer, Integer maxScore) throws InterruptedIOException {
        //1.生成ai调用的提示词
        String gradingPrompt = buildGradingPrompt(question, userAnswer, maxScore);
        //2.调用ai模型，获取返回结果
        String content = callAi(gradingPrompt);
        //3.进行结果的解析 -》GradingResult
    /*
            prompt.append("{\n");
            prompt.append("  \"score\": 实际得分(整数),\n");
            prompt.append("  \"feedback\": \"具体的评价反馈(50字以内)\",\n");
            prompt.append("  \"reason\": \"扣分原因或得分依据(30字以内)\"\n");
            prompt.append("}");
         */
        com.alibaba.fastjson2.JSONObject jsonObject = JSON.parseObject(content);
        Integer aiScore = jsonObject.getInteger("score");
        String feedback = jsonObject.getString("feedback");
        String reason = jsonObject.getString("reason");
        if (aiScore > maxScore) aiScore = maxScore;
        if (aiScore < 0) aiScore = 0;
        return  new GradingResult(aiScore, feedback, reason);
    }

    /**
     * 构建摘要信息的方法
     * @param totalScore 总得分
     * @param maxScore 最大得分
     * @param questionCount 题目总数
     * @param correctCount 正确答题数
     * @return 返回构建的摘要字符串
     * @throws InterruptedIOException 当IO操作被中断时抛出此异常
     */
    @Override
    public String buildSummary(Integer totalScore, Integer maxScore, Integer questionCount, Integer correctCount) throws InterruptedIOException {
        String summaryPrompt = buildSummaryPrompt(totalScore, maxScore, questionCount, correctCount);
        return callAi(summaryPrompt);
    }

    /**
     * 获取题目类型文本
     */
    private String getQuestionTypeText(String type) {
        Map<String, String> typeMap = new HashMap<>();
        typeMap.put("CHOICE", "选择题");
        typeMap.put("JUDGE", "判断题");
        typeMap.put("TEXT", "简答题");
        return typeMap.getOrDefault(type, "未知题型");
    }

    private String callAi(String prompt) {
        int maxTry = 3; // 最多重试3次
        for (int i = 1; i <= maxTry; i++) {
            try {
                // 使用 Spring AI ChatClient 发起请求
                ChatResponse response = chatClient.prompt()
                        .user(prompt) // 设置用户提示词
                        .call() // 同步调用（替代原block()）
                        .chatResponse(); // 获取完整响应

                // 解析响应结果
                String content = null;
                if (response != null) {
                    content = response.getResult().getOutput().getText();
                }
                log.debug("调用AI返回的结果为：{}", content);

                // 校验返回内容
                if (content == null || content.isEmpty()) {
                    throw new RuntimeException("调用成功！但是没有返回结果！！");
                }
                return content;

            } catch (Exception e) {
                // 记录失败日志
                log.debug("第{}次尝试调用失败！异常信息：{}", i, e.getMessage());

                // 重试间隔1秒
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // 恢复中断状态
                    log.error("线程休眠被中断", ie);
                }

                // 达到最大重试次数则抛出最终异常
                if (i == maxTry) {
                    log.error("已重试{}次！依然失败！", maxTry, e);
                    throw new RuntimeException("已经重试3次！依然失败！请稍后再试！！", e);
                }
            }
        }
        // 理论上不会走到这里，兜底异常
        throw new RuntimeException("已经重试3次！依然失败！请稍后再试！！");
    }
}
