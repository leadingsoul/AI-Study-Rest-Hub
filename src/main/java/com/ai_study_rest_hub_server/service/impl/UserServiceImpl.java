package com.ai_study_rest_hub_server.service.impl;

import com.ai_study_rest_hub_server.config.properties.JwtProperties;
import com.ai_study_rest_hub_server.constant.JwtClaimsConstant;
import com.ai_study_rest_hub_server.dto.LoginRequest;
import com.ai_study_rest_hub_server.dto.LoginResponse;
import com.ai_study_rest_hub_server.utils.JwtUtils;
import com.ai_study_rest_hub_server.utils.RedisUtils;
import com.ai_study_rest_hub_server.utils.SecureUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ai_study_rest_hub_server.entity.User;
import com.ai_study_rest_hub_server.service.UserService;
import com.ai_study_rest_hub_server.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
* @author 刘博啸辉
* @description 针对表【users】的数据库操作Service实现
* @createDate 2026-01-01 17:14:20
*/
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    private final RedisUtils redisUtils;
    private final JwtProperties jwtProperties;

    @Override
    public User login(String username, String password) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        queryWrapper.eq(User::getStatus, "active");
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean isAdmin(Long userId) {
        User user = this.getById(userId);
        return user != null && user.getRole().equals("admin");
    }

    @Override
    public LoginResponse adminLogin(LoginRequest loginRequest) {
        // 验证参数
        if (loginRequest.getUsername() == null || loginRequest.getUsername().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }

        // 执行登录
        User user = login(loginRequest.getUsername(), loginRequest.getPassword());
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        String salt = user.getSalt();
        String md5Password = user.getPassword();
        String input = loginRequest.getPassword();
        if (!SecureUtils.checkPassword(input,salt,md5Password)){
            throw new RuntimeException("用户名或密码错误");
        }
        // 登录成功后，生成token令牌,并用redis存储token以及用户信息和过期时间
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());
        claims.put(JwtClaimsConstant.ROLE,user.getRole());
        String token = JwtUtils.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims
        );
        long expireSeconds = jwtProperties.getAdminTtl() / 1000;
        redisUtils.set("TOKEN:" + token, user, expireSeconds);
        // 构建登录响应
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRole(user.getRole());
        response.setToken(token);
        return response;
    }

    @Override
    public void adminLogOut(HttpServletRequest request){
        // 从请求头获取 token（优先取 token，取不到再取 Authorization）
        String token = request.getHeader("token");

        // 如果 token 为 null，尝试从 Authorization 取（兼容标准格式）
        if (token == null) {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                token = auth.substring(7);
            }
        }
        redisUtils.delete("TOKEN:" + token);
    }
}




