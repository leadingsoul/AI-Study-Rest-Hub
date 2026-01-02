package com.ai_study_rest_hub_server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ai_study_rest_hub_server.entity.Category;
import com.ai_study_rest_hub_server.service.CategoryService;
import com.ai_study_rest_hub_server.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

/**
* @author 刘博啸辉
* @description 针对表【categories】的数据库操作Service实现
* @createDate 2026-01-01 17:11:12
*/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService {

}




