package com.ai_study_rest_hub_server;

import com.ai_study_rest_hub_server.entity.User;
import com.ai_study_rest_hub_server.service.UserService;
import com.ai_study_rest_hub_server.utils.SecureUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest

class AiStudyRestHubServerApplicationTests {


    public static void main(String[] args){
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, "admin");
        queryWrapper.eq(User::getStatus, "active");
        String salt = "0b94f5331f0848128b05a33cc6a73132";
        String password = "17ef601b91d5b7c257df2d43cb5d7274";
        String input = "admin";
        if(SecureUtils.checkPassword(input,salt,password)){
            System.out.println("密码正确");
        }
        else {
            System.out.println("密码错误");
        }



    }

}
