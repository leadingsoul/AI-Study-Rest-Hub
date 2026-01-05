package com.ai_study_rest_hub_server.mapper;

import com.ai_study_rest_hub_server.entity.VideoView;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
* @author leadingsoul
* @description 针对表【video_views(视频观看记录表)】的数据库操作Mapper
* @createDate 2026-01-01 17:14:44
* @Entity com.ai_study_rest_hub_server.entity.VideoViews
*/
public interface VideoViewMapper extends BaseMapper<VideoView> {

    @Select("SELECT COUNT(*) FROM video_views WHERE video_id = #{videoId}")
    Long getViewCountByVideoId(Long videoId);

    @Select("SELECT AVG(view_duration) FROM video_views WHERE video_id = #{videoId}")
    Double getAverageViewDuration(Long videoId);

    @Select("SELECT DATE(created_at) as view_date, COUNT(*) as view_count " +
            "FROM video_views " +
            "WHERE video_id = #{videoId} AND created_at >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY view_date DESC")
    List<Map<String, Object>> getViewStatsByDate(Long videoId, Integer days);
}




