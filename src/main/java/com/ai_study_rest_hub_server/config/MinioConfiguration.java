package com.ai_study_rest_hub_server.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.ai_study_rest_hub_server.config.properties.MinioProperties;

@EnableConfigurationProperties(MinioProperties.class)
@Slf4j
@RequiredArgsConstructor
@Configuration
public class MinioConfiguration {

    private final MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getEndPoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
        log.info("MinioClient初始化成功,链接对象为：{}", minioClient);
        return minioClient;
    }
}
