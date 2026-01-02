package com.ai_study_rest_hub_server.service;

import com.ai_study_rest_hub_server.entity.Banner;
import com.baomidou.mybatisplus.extension.service.IService;
import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
* @author leadingsoul
* @description 针对表【banners(轮播图表)】的数据库操作Service
* @createDate 2026-01-01 17:08:36
*/
public interface BannerService extends IService<Banner> {

    /**
     * 添加轮播图的方法
     * @param banner 要添加的轮播图对象
     */
    void addBanner(Banner banner);

    /**
     * 更新轮播图信息的方法
     * @param banner 包含更新后轮播图信息的Banner对象
     */
    void updateBanner(Banner banner);

    /**
     * 上传轮播图片的方法
     *
     * @param file 要上传的文件，类型为MultipartFile
     * @return 返回一个String类型的结果，可能是上传后文件的URL或标识符
     * @throws ServerException 服务器异常
     * @throws InsufficientDataException 数据不完整异常
     * @throws ErrorResponseException 错误响应异常
     * @throws IOException IO异常
     * @throws NoSuchAlgorithmException 没有对应的算法异常
     * @throws InvalidKeyException 无效的密钥异常
     * @throws InvalidResponseException 无效的响应异常
     * @throws XmlParserException XML解析异常
     * @throws InternalException 内部异常
     */
    String uploadBannerImage(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;
}
