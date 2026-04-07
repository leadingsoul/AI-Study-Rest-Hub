package com.ai_study_rest_hub_server.interceptor;

import com.ai_study_rest_hub_server.common.Result;
import com.ai_study_rest_hub_server.config.properties.JwtProperties;
import com.ai_study_rest_hub_server.constant.JwtClaimsConstant;
import com.ai_study_rest_hub_server.context.BaseContext;
import com.ai_study_rest_hub_server.utils.JwtUtils;
import com.ai_study_rest_hub_server.utils.RedisUtils;
import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenAdminInterceptor implements HandlerInterceptor {
    private final JwtProperties jwtProperties;
    private final RedisUtils redisUtils;

    /**
     * 校验jwt
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }
        String token = request.getHeader("token");

        // 如果 token 为 null，尝试从 Authorization 取（兼容标准格式）
        if (token == null) {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                token = auth.substring(7);
            }
        }
        // 取不到 token → 直接返回未登录
        if (token == null || token.isBlank()) {
            return errorResponse(response, "请先登录");
        }
        // 4. 去 Redis 校验 token 是否存在
        Object user = redisUtils.get("TOKEN:" + token);
        if (user == null) {
            return errorResponse(response, "登录已过期，请重新登录");
        }
        // 5. 校验通过，放行
        return true;
    }
    // 统一返回错误信息
    private boolean errorResponse(HttpServletResponse response, String msg) throws Exception {
        Result result = Result.error(msg);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JSONObject.toJSONString(result));
        return false;
    }
}
