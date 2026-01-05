package com.ai_study_rest_hub_server.controller;

import com.ai_study_rest_hub_server.common.Result;
import com.ai_study_rest_hub_server.entity.GuessCategory;
import com.ai_study_rest_hub_server.mapper.GuessCategoryMapper;
import com.ai_study_rest_hub_server.service.GuessCategorieService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/api/guess/category")
public class GuessCategoryController {

    private final GuessCategorieService guessCategorieService;

    /**
     * 获取分类列表（包含视频数量）
     * @return 分类列表数据
     */
    @GetMapping("/tree")
    @Operation(summary = "获取分类树形结构",description = "加载分类管理页面的层级分类数据，支持树形展示。")
    public Result<List<GuessCategory>> getGuessCategories(){
        return Result.success(guessCategorieService.getGuessCategories());
    }

    /**
     * 保存分类信息的接口方法
     *
     * @PostMapping("/save") 指定该方法的请求路径为"/save"，且只接收POST请求
     * @Operation(summary = "保存分类",description = "保存分类信息，包括新增和修改。")
     *     用于接口文档说明，summary是简短描述，description是详细描述
     *
     * @param guessCategory 接收前端传递的JSON数据，并将其自动转换为GuessCategory对象
     *
     * @return 返回一个操作结果，成功时返回success状态码和null数据
     */
    @PostMapping("/save")
    @Operation(summary = "保存分类",description = "保存分类信息，包括新增和修改。")
    public Result<Void> save(@RequestBody GuessCategory guessCategory) {
    // 调用服务层的saveCategory方法，保存或更新分类信息
        guessCategorieService.saveCategory(guessCategory);
        return Result.success(null);
    }

    /**
     * 更新分类接口
     * 根据传入的ID和分类信息，更新对应的分类数据
     *
     * @param id 分类ID，用于标识需要更新的分类
     * @param guessCategory 包含分类更新信息的实体类，如名称、描述、排序等
     * @return 返回操作结果，成功时返回success状态
     */
    @PostMapping("/update/{id}")
    @Operation(summary = "更新分类",description = "更新分类信息，包括名称、描述、排序等。")
    public Result<Void> update(@PathVariable Long id, @RequestBody GuessCategory guessCategory) {
        guessCategorieService.updateCategory(id, guessCategory);
        return Result.success(null);
    }


    /**
     * 删除分类接口
     * 根据传入的分类ID，删除对应的分类数据
     *
     * @param id 分类ID，用于标识需要删除的分类
     * @return 返回操作结果，成功时返回success状态
     */
    @PostMapping("/delete/{id}")
    @Operation(summary = "删除分类",description = "删除指定ID的分类。")
    public Result<Void> delete(@PathVariable Long id) {
        guessCategorieService.deleteCategory(id);
        return Result.success(null);

    }
}
