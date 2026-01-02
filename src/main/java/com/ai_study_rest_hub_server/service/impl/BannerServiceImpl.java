package com.ai_study_rest_hub_server.service.impl;

import com.ai_study_rest_hub_server.constant.StatusConstant;
import com.ai_study_rest_hub_server.service.FileUploadService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ai_study_rest_hub_server.entity.Banner;
import com.ai_study_rest_hub_server.service.BannerService;
import com.ai_study_rest_hub_server.mapper.BannerMapper;
import io.minio.errors.*;
import io.netty.util.internal.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
* @author 刘博啸辉
* @description 针对表【banners(轮播图表)】的数据库操作Service实现
* @createDate 2026-01-01 17:08:36
*/
@Slf4j
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner>
    implements BannerService {

    @Autowired
    private FileUploadService fileUploadService;

    /**
 * 添加横幅的方法
 * @param banner 要添加的横幅对象
 */
    public void addBanner(Banner banner) {
        //1.确认banner createTime和updateTime有时间
        //方式1：数据库设置时间  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
        //方案2：代码时间赋值   set new Date();
        //方案3：使用mybatis-plus自动填充功能 [知识点中会说明]
        //2.判断下启动状态
        if (banner.getIsActive() == null){
            banner.setIsActive(true);
        }
        //3.判断优先级
        if (banner.getSortOrder() == null){
            banner.setSortOrder(0);
        }
        //4.进行保存
        boolean isSuccess = save(banner);

        if (!isSuccess) {
            throw new RuntimeException("轮播图保存失败！");
        }

        log.info("轮播图保存成功！！");
    }


    /**
     * 更新横幅信息的方法
     * @param banner 包含更新后横幅信息的对象
     */
    public void updateBanner(Banner banner) {
        boolean success = updateById(banner);
        if (!success) {
            throw new RuntimeException("轮播图更新失败！");
        }
    }

    public String uploadBannerImage(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        if(file.isEmpty()){
            throw new RuntimeException("上传文件不能为空");
        }
        String contentType = file.getContentType();
        if(ObjectUtils.isEmpty(contentType)||!contentType.startsWith("image")){
            throw new RuntimeException("上传文件类型错误");
        }
        String imageUrl = fileUploadService.uploadFile(file,"banners");
        return imageUrl;
    }
}




