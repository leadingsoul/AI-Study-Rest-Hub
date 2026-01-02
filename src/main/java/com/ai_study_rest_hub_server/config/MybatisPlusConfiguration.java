package com.ai_study_rest_hub_server.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.ai_study_rest_hub_server.mapper")
public class MybatisPlusConfiguration {
}
