package com.ai_study_rest_hub_server.controller;

import com.ai_study_rest_hub_server.common.Result;
import com.ai_study_rest_hub_server.constant.GuessPromptTemplate;
import com.ai_study_rest_hub_server.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/api/guess")
public class GuessController {

    private final AIService aiService;

    @PostMapping("/chat")
    @Operation(summary = "AI对话",description = "用户与AI对话获得提示")
    public Result<Map<String, Object>> chat(
            @Parameter(description = "题目ID") @RequestParam Long topicId,
            @Parameter(description = "用户消息") @RequestParam String message,
            @Parameter(description = "顶级分类编码（如DISEASE/ITEM/PERSON）") @RequestParam String topCategoryCode,
            @Parameter(description = "目标答案（需要猜测的内容）") @RequestParam String target,
            @Parameter(description = "对话历史") @RequestBody(required = false) List<Map<String, String>> chatHistory
    ) {

        Map<String, Object> result = aiService.chat(topicId, message, topCategoryCode, target, chatHistory);
        return Result.success(result);
    }
}
