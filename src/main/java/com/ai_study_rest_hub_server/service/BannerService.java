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

    void addBanner(Banner banner);

    void updateBanner(Banner banner);

    String uploadBannerImage(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;
}
