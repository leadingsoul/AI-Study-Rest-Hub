package com.ai_study_rest_hub_server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ai_study_rest_hub_server.entity.Video;
import com.ai_study_rest_hub_server.service.VideoService;
import com.ai_study_rest_hub_server.mapper.VideoMapper;
import org.springframework.stereotype.Service;

/**
* @author 刘博啸辉
* @description 针对表【videos(视频信息表)】的数据库操作Service实现
* @createDate 2026-01-01 17:14:54
*/
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video>
    implements VideoService {

}




