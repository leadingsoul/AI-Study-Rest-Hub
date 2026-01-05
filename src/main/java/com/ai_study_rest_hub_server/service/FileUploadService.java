package com.ai_study_rest_hub_server.service;

import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 文件上传服务
 * 支持MinIO和本地文件存储两种办法
 *
 * */
public interface FileUploadService {
    /**
     * 上传文件（自动选择MinIO或本地存储）
     * @param file 上传的文件
     * @param folder 文件夹名称（如：banners, avatars等）
     * @return 返回图片可访问地址
     */
    String uploadFile(MultipartFile file, String folder) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    /**
     * 删除文件的方法
     * @param fileUrl 文件的URL路径
     * @return 删除成功返回true，删除失败返回false
     */
    boolean deleteFile(String fileUrl);
}
