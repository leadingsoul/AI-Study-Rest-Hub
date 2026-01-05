package com.ai_study_rest_hub_server.mapper;

import com.ai_study_rest_hub_server.entity.VideoLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
* @author leadingsoul
* @description 针对表【video_likes(视频点赞表)】的数据库操作Mapper
* @createDate 2026-01-01 17:14:36
* @Entity com.ai_study_rest_hub_server.entity.VideoLikes
*/
public interface VideoLikeMapper extends BaseMapper<VideoLike> {

    /**
     * 判断指定ID的内容是否被指定IP的用户点赞
     * @param id 需要判断的内容ID
     * @param userIp 点赞用户的IP地址
     * @return 如果用户已点赞则返回true，否则返回false
     */
    @Select("select count(*)>0 from video_likes where video_id=#{id} and user_ip=#{userIp}")
    boolean isLikedById(Long id, String userIp);

    @Select("SELECT COUNT(*) FROM video_likes WHERE video_id = #{videoId}")
    Long getLikeCountByVideoId(Long videoId);
}




