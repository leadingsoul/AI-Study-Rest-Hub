package com.ai_study_rest_hub_server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@Slf4j
@SpringBootApplication
public class AiStudyRestHubServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiStudyRestHubServerApplication.class, args);
        log.info("项目启动成功");
    }

}
