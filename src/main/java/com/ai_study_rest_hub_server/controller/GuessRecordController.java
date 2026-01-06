package com.ai_study_rest_hub_server.controller;

import com.ai_study_rest_hub_server.common.Result;
import com.ai_study_rest_hub_server.entity.GuessRecord;
import com.ai_study_rest_hub_server.service.GuessRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/api/guess/record")
public class GuessRecordController {

    private final GuessRecordService guessRecordService;

    @PostMapping("/save")
    @Operation(summary = "保存猜测记录",description = "用户猜对词后，保存本次猜词的记录（次数、耗时、是否成功等）")
    public Result<Void> saveRecord(@Parameter(description = "题目") @RequestBody GuessRecord guessRecord){
        guessRecordService.saveRecord(guessRecord);
        return Result.success(null);
    }
}
