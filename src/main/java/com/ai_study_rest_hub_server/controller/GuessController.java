package com.ai_study_rest_hub_server.controller;

import com.ai_study_rest_hub_server.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/guess")
public class GuessController {

    private final ChatClient chatClient;

    @PostMapping("/chat")
    @Operation(summary = "AI对话",description = "用户与AI对话获得提示")
    public Result<String> chat(String message) {
        return Result.success(chatClient.prompt().user(message).call().content());
    }
}
