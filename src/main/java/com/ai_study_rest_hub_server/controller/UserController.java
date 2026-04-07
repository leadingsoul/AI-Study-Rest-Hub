package com.ai_study_rest_hub_server.controller;


import com.ai_study_rest_hub_server.common.Result;
import com.ai_study_rest_hub_server.dto.LoginRequest;
import com.ai_study_rest_hub_server.dto.LoginResponse;
import com.ai_study_rest_hub_server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * 用户控制器 - 处理用户认证和权限管理相关的HTTP请求
 * 包括用户登录、权限验证等功能
 */
@Slf4j
@RequiredArgsConstructor
@RestController  // REST控制器，返回JSON数据
@RequestMapping("/api/user")  // 用户API路径前缀
@CrossOrigin(origins = "*")  // 允许跨域访问
@Tag(name = "用户管理", description = "用户相关操作，包括登录认证、权限验证等功能")  // Swagger API分组
public class UserController {
    private final UserService userService;
    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 登录结果
     */
    @PostMapping("/login")  // 处理POST请求
    @Operation(summary = "用户登录", description = "用户通过用户名和密码进行登录验证，返回用户信息和token")  // API描述
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = userService.adminLogin(loginRequest);
        return Result.success(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出",description = "用户退出登录状态，删除redis中token信息")
    public Result<Void> logout(HttpServletRequest request){
        userService.adminLogOut(request);
        return Result.success("登出成功");
    }

    /**
     * 检查用户权限
     * @param userId 用户ID
     * @return 权限检查结果
     */
    @GetMapping("/check-admin/{userId}")  // 处理GET请求
    @Operation(summary = "检查管理员权限", description = "验证指定用户是否具有管理员权限")  // API描述
    public Result<Boolean> checkAdmin(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        boolean isAdmin = userService.isAdmin(userId);
        return Result.success(isAdmin);
    }
} 