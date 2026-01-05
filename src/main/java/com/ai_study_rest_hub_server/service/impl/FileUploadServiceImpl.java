package com.ai_study_rest_hub_server.service.impl;

import com.ai_study_rest_hub_server.config.properties.MinioProperties;
import com.ai_study_rest_hub_server.service.FileUploadService;
import io.minio.*;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
        //3. 处理上传的对象名（影响，minio桶中的文件结构！）
        //现在： 桶名 / folder / ai.png  缺点： 所有文件都平铺（banner，video）不好区分！ 核心缺点，可能覆盖！
        //小知识点： x/x/x.png -> exam0625 /x/x/ x.png
        //解决覆盖问题： 确保对象和文件的名字唯一即可！！ uuid - - -
        //1.需要添加文件夹 2.添加uuid确保不重复
        String objectName = folder + "/" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + "/" +
                UUID.randomUUID().toString().replaceAll("-","")+"_"+ file.getOriginalFilename();

        log.debug("文件上传核心业务方法，处理后的文件对象名：{}",objectName);

        //4. 上传文件 putObject方法
        //putObject . 上传文件数据 .steam(文件输入流)
        //uploadObject .上传文件数据 .filename(文件的磁盘地址 c:\\)
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioProperties.getBucketName())
                .contentType(file.getContentType())
                .object(objectName) //对象
                .stream(file.getInputStream(),file.getSize(),-1) //-1 我们不指定文件切割大小！让minio自动处理！
                .build());

        //5. 拼接回显地址 【端点 + 桶 + 对象名】
        String url = String.join("/", minioProperties.getEndPoint(), minioProperties.getBucketName(), objectName);
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
            // 1. 解析URL，提取objectName（去掉端点和桶名部分）
            String basePath = minioProperties.getEndPoint() + "/" + minioProperties.getBucketName() + "/";
            if (!fileUrl.startsWith(basePath)) {
                log.error("文件URL格式错误，无法解析：{}", fileUrl);
                return false;
            }
            String objectName = fileUrl.substring(basePath.length());
            log.debug("准备删除MinIO文件，对象名：{}", objectName);

            // 2. 检查对象是否存在（可选，但建议做）
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

            // 3. 执行删除操作
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
