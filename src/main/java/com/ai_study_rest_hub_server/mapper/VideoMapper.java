package com.ai_study_rest_hub_server.mapper;

import com.ai_study_rest_hub_server.entity.Video;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
* @author leadingsoul
* @description 针对表【videos(视频信息表)】的数据库操作Mapper
* @createDate 2026-01-01 17:14:54
* @Entity com.ai_study_rest_hub_server.entity.Videos
*/
public interface VideoMapper extends BaseMapper<Video> {

    IPage<Video> getPublishedVideos(Page<Video> pageObj, Long categoryId, String keyword);

    List<Video> getPopularVideos(Integer limit);

    List<Video> getLatestVideos(Integer limit);

    @Update("update videos set view_count = view_count + 1 where id = #{videoId}")
    void incrementViewCount(Long videoId);
    @Update("update videos set like_count = like_count - 1 where id = #{videoId} and like_count > 0")
    void decrementLikeCount(Long videoId);
    @Update("update videos set like_count = like_count + 1 where id = #{videoId}")
    void incrementLikeCount(Long videoId);

    IPage<Video> getVideosForAdmin(Page<Video> pageObj, Integer status, Integer uploaderType, String keyword);

    @Select("SELECT " +
            "COUNT(*) as total_count, " +
            "COUNT(CASE WHEN status = 0 THEN 1 END) as pending_count, " +
            "COUNT(CASE WHEN status = 1 THEN 1 END) as published_count, " +
            "COUNT(CASE WHEN status = 2 THEN 1 END) as rejected_count, " +
            "COUNT(CASE WHEN uploader_type = 1 THEN 1 END) as user_upload_count, " +
            "COUNT(CASE WHEN uploader_type = 2 THEN 1 END) as admin_upload_count " +
            "FROM videos")
    Map<String, Object> getVideoStatistics();
}




