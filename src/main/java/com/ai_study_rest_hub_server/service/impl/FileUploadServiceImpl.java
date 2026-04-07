package com.ai_study_rest_hub_server.service.impl;

import com.ai_study_rest_hub_server.config.properties.MinioProperties;
import com.ai_study_rest_hub_server.service.FileUploadService;
import io.minio.*;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final MinioClient minioClient;

    private final MinioProperties minioProperties;
    public String uploadFile(MultipartFile file,String folder) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        //1. 判断桶是否存在
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build());
        //2. 不存在，创建桶，同时设置访问权限
        if (!bucketExists) {
            //创建桶
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build());
            String config = """
                        {
                              "Statement" : [ {
                                "Action" : "s3:GetObject",
                                "Effect" : "Allow",
                                "Principal" : "*",
                                "Resource" : "arn:aws:s3:::%s/*"
                              } ],
                              "Version" : "2012-10-17"
                        }
                    """.formatted(minioProperties.getBucketName());
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .config(config)
                    .build());
        }
        // 3. 处理上传的对象名（核心优化：确保路径符合MinIO规范，避免非法字符和连续斜杠）
        // 3.1 清理文件夹参数：去除首尾斜杠、替换连续斜杠为单个斜杠
        String cleanFolder = folder.trim().replaceAll("/+", "/").replaceAll("^/|/$", "");
        // 3.2 获取日期路径（按年月日归档）
        String datePath = new SimpleDateFormat("yyyyMMdd").format(new Date());
        // 3.3 处理文件名：获取原始文件名，避免空文件名，过滤非法字符
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            originalFilename = "unnamed_file_" + System.currentTimeMillis();
            log.warn("文件原始名称为空，使用默认文件名：{}", originalFilename);
        }
        // 过滤文件名中的MinIO不支持的非法字符（核心：避免XMinioInvalidObjectName错误）
        String validFilename = originalFilename.replaceAll("[\\\\:*?\"<>|^`%#]", "");
        // 3.4 生成唯一标识（UUID），防止文件覆盖
        String uniqueId = UUID.randomUUID().toString().replaceAll("-", "");
        // 3.5 拼接对象名并最终清理路径（确保无连续斜杠，符合MinIO规范）
        String objectName = String.join("/", cleanFolder, datePath, uniqueId + "_" + validFilename)
                .replaceAll("/+", "/"); // 最终兜底，去除所有连续斜杠

        log.debug("文件上传核心业务方法，处理后的合法文件对象名：{}", objectName);

        //4. 上传文件 putObject方法
        //putObject . 上传文件数据 .steam(文件输入流)
        //uploadObject .上传文件数据 .filename(文件的磁盘地址 c:\\)
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioProperties.getBucketName())
                .contentType(file.getContentType())
                .object(objectName) //对象
                .stream(file.getInputStream(),file.getSize(),-1) //-1 我们不指定文件切割大小！让minio自动处理！
                .build());

        // 5. 拼接回显地址（核心优化：使用合规拼接方式，避免连续斜杠，兼容端点末尾带/或不带/的情况）
        // 5.1 清理端点地址（去除末尾多余斜杠）
        String cleanEndPoint = minioProperties.getEndPoint().trim().replaceAll("/+$", "");
        // 5.2 合规拼接URL（端点 + 桶名 + 对象名，无连续斜杠）
        String url = String.join("/", cleanEndPoint, minioProperties.getBucketName(), objectName);
        log.info("文件上传核心业务，完成{}文件上传，返回地址为：{}",objectName,url);
        return url;
    }

    /**
     * 删除MinIO桶内指定文件
     * @param fileUrl 上传文件时返回的完整URL
     * @return 是否删除成功
     */
    public boolean deleteFile(String fileUrl) {
        try {
            // 1. 校验入参
            if (!StringUtils.hasText(fileUrl)) {
                log.error("删除文件URL为空，无法执行删除操作");
                return false;
            }

            // 2. 解析URL，提取objectName（优化：兼容端点末尾带/或不带/的情况）
            String cleanEndPoint = minioProperties.getEndPoint().trim().replaceAll("/+$", "");
            String basePath = String.join("/", cleanEndPoint, minioProperties.getBucketName()) + "/";
            if (!fileUrl.startsWith(basePath)) {
                log.error("文件URL格式错误，无法解析：{}", fileUrl);
                return false;
            }
            String objectName = fileUrl.substring(basePath.length());
            log.debug("准备删除MinIO文件，对象名：{}", objectName);

            // 3. 检查对象是否存在（可选，但建议做）
            boolean objectExists = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .build()
            ) != null;

            if (!objectExists) {
                log.warn("文件不存在，无需删除：{}", objectName);
                return false;
            }

            // 4. 执行删除操作
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .build()
            );

            log.info("成功删除MinIO文件：{}", objectName);
            return true;

        } catch (Exception e) {
            log.error("删除MinIO文件失败：{}", fileUrl, e);
            return false;
        }
    }
}
