package com.ai_study_rest_hub_server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ai_study_rest_hub_server.entity.Notice;
import com.ai_study_rest_hub_server.service.NoticeService;
import com.ai_study_rest_hub_server.mapper.NoticeMapper;
import org.springframework.stereotype.Service;

/**
* @author 刘博啸辉
* @description 针对表【notices(公告表)】的数据库操作Service实现
* @createDate 2026-01-01 17:12:54
*/
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice>
    implements NoticeService {

}




