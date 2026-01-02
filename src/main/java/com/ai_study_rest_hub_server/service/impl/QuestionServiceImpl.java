package com.ai_study_rest_hub_server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ai_study_rest_hub_server.entity.Question;
import com.ai_study_rest_hub_server.service.QuestionService;
import com.ai_study_rest_hub_server.mapper.QuestionMapper;
import org.springframework.stereotype.Service;

/**
* @author 刘博啸辉
* @description 针对表【questions】的数据库操作Service实现
* @createDate 2026-01-01 17:14:13
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService {

}




