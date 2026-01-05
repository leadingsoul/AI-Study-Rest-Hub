package com.ai_study_rest_hub_server.service;

import com.ai_study_rest_hub_server.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotBlank;

/**
* @author leadingsoul
* @description 针对表【users】的数据库操作Service
* @createDate 2026-01-01 17:14:20
*/
public interface UserService extends IService<User> {

    /**
     * 用户登录方法
     * @param username 用户名，不能为空
     * @param password 密码，不能为空
     * @return 登录成功的用户对象
     */
    User login(@NotBlank(message = "用户名不能为空") String username, @NotBlank(message = "密码不能为空") String password);

    /**
     * 判断用户是否为管理员
     * @param userId 用户ID，用于标识唯一用户
     * @return 如果用户是管理员返回true，否则返回false
     */
    boolean isAdmin(Long userId);
}
