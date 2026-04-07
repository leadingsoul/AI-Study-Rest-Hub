package com.ai_study_rest_hub_server.config;

import com.ai_study_rest_hub_server.utils.PromptUtils;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class AIConfiguration {

    public final PromptUtils promptUtils;

    public DashScopeApi dashScopeApi = DashScopeApi.builder()
            .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
            .build();

    public ChatModel chatModelForExam = DashScopeChatModel.builder()
            .dashScopeApi(dashScopeApi)
            .defaultOptions(DashScopeChatOptions.builder()
                    .model("deepseek-v3")
                    .temperature(0.3)
                    .maxToken(2048)
                    .build())
            .build();

    @Bean
    public ChatClient chatClient(DashScopeChatModel model){
        return ChatClient.builder(model).build();
    }

    ReactAgent examGradingAgent = ReactAgent.builder()
            .name("评卷小助手")
            .model(chatModelForExam)
            .systemPrompt(promptUtils.getPrompt("examGradingAgent.txt"))
            .build();

}
