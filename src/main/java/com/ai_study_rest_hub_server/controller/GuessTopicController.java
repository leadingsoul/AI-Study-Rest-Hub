package com.ai_study_rest_hub_server.controller;

import com.ai_study_rest_hub_server.common.Result;
import com.ai_study_rest_hub_server.entity.GuessTopic;
import com.ai_study_rest_hub_server.service.GuessTopicService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/api/guess/topic")
public class GuessTopicController {

    private final GuessTopicService guessTopicService;

    @GetMapping()
    @Operation(summary = "获取题目列表（分页 + 筛选）",description = "加载题目管理页面的题目列表，支持分类、难度、关键词筛选及分页")
    public Result<IPage<GuessTopic>> getTopicList(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小，默认10") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "猜词分类id") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "难度（EASY/MEDIUM/HARD）") @RequestParam(required = false) String difficulty,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword) {
        IPage<GuessTopic> result = guessTopicService.getTopicList(page, size, categoryId, difficulty, keyword);
        return Result.success(result);
    }

    @PostMapping("/save")
    @Operation(summary = "新增题目",description = "手动创建新的猜词题目")
    public Result<Void> saveTopic(@RequestBody GuessTopic guessTopic) {
        guessTopicService.saveTopic(guessTopic);
        return Result.success(null);
    }

    @PostMapping("/update/{id}")
    @Operation(summary = "更新题目",description = "根据题目ID更新题目信息")
    public Result<Void> updateTopic(@PathVariable Long id, @RequestBody GuessTopic guessTopic) {
        guessTopicService.updateTopic(id, guessTopic);
        return Result.success(null);
    }

    @PostMapping("/delete/{id}")
    @Operation(summary = "删除题目",description = "根据题目ID删除题目")
    public Result<Void> deleteTopic(@PathVariable Long id) {
        guessTopicService.deleteTopic(id);
        return Result.success(null);
    }
}
