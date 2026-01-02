package com.ai_study_rest_hub_server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ai_study_rest_hub_server.entity.User;
import com.ai_study_rest_hub_server.service.UserService;
import com.ai_study_rest_hub_server.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author 刘博啸辉
* @description 针对表【users】的数据库操作Service实现
* @createDate 2026-01-01 17:14:20
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

}




