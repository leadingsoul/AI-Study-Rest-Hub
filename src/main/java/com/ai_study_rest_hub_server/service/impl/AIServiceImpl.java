package com.ai_study_rest_hub_server.service.impl;

import com.ai_study_rest_hub_server.common.Result;
import com.ai_study_rest_hub_server.constant.GuessPromptTemplate;
import com.ai_study_rest_hub_server.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * AI服务实现类
 * */
@Slf4j
@RequiredArgsConstructor
@Service
public class AIServiceImpl implements AIService {

    private final ChatClient chatClient;

    @Override
    public Map<String, Object> chat(Long topicId, String message, String topCategoryCode, String target, List<Map<String, String>> chatHistory) {
        // 1. 获取对应分类的提示词模板
        String template = GuessPromptTemplate.getTemplateByCategory(topCategoryCode);

        // 2. 格式化对话历史为字符串
        String chatHistoryStr = formatChatHistory(chatHistory);

        // 3. 填充模板变量
        String prompt = template.replace("{target}", target)
                .replace("{chatHistory}", chatHistoryStr);

        // 4. 拼接用户当前消息
        prompt += "\n\n用户当前提问：" + message;

        log.info("生成的AI提示词：{}", prompt);

        // 5. 调用AI并获取响应
        String aiResponse = callAi(prompt);

        // 6. 判断是否猜对（AI回复包含success标识）
        boolean isCorrect = aiResponse.contains("success");
        String finalResponse = isCorrect ? "恭喜你猜对了！答案就是「" + target + "」" : aiResponse.replace("success", "").trim();

        // 7. 返回结果（包含是否猜对的标识和回复内容）
        return Map.of(
                "content", finalResponse,
                "isCorrect", isCorrect
        );
    }

    /**
     * 格式化对话历史为可读字符串
     */
    private String formatChatHistory(List<Map<String, String>> chatHistory) {
        // 如果对话历史为空，则返回提示信息
        if (chatHistory == null || chatHistory.isEmpty()) {
            return "无对话历史";
        }
        // 拼接对话历史字符串
        StringBuilder sb = new StringBuilder();
        for (Map<String, String> msg : chatHistory) {
            String role = "user".equals(msg.get("role")) ? "用户" : "AI";
            String content = msg.get("content") == null ? "" : msg.get("content");
            sb.append(role).append("：").append(content).append("；");
        }
        return sb.toString().replaceAll("；$", ""); // 移除最后一个分号
    }

    private String callAi(String prompt) {
        int maxTry = 3; // 最多重试3次
        for (int i = 1; i <= maxTry; i++) {
            try {
                // 使用 Spring AI ChatClient 发起请求
                ChatResponse response = chatClient.prompt()
                        .user(prompt) // 设置用户提示词
                        .call()
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
