package com.ai_study_rest_hub_server.service;

import com.ai_study_rest_hub_server.entity.Video;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
* @author leadingsoul
* @description 针对表【videos(视频信息表)】的数据库操作Service
* @createDate 2026-01-01 17:14:54
*/
public interface VideoService extends IService<Video> {

    /**
     * 获取已发布的视频列表
     *
     * @param page 页码，用于分页查询
     * @param size 每页大小，用于分页查询
     * @param categoryId 分类ID，用于按分类筛选视频
     * @param keyword 关键词，用于搜索视频标题或描述
     * @param request HTTP请求对象，可以用于获取请求头、会话等信息
     * @return 返回一个IPage<Video>对象，包含分页信息和视频数据列表
     */
    IPage<Video> getPublishedVideos(Integer page, Integer size, Long categoryId, String keyword, HttpServletRequest request);

    /**
     * 获取视频详情信息
     * @param id 视频ID，用于标识要查询的具体视频
     * @param request HttpServletRequest对象，包含请求相关的所有信息
     * @return Video 返回视频详情对象，包含视频的完整信息
     */
    Video getVideoDetail(Long id, HttpServletRequest request);

    /**
     * 获取热门视频列表
     *
     * @param limit 返回的视频数量限制
     * @return 包含热门视频的列表，列表大小不超过limit指定的值
     */
    List<Video> getPopularVideos(Integer limit);

    /**
     * 获取最新视频列表的方法
     * @param limit 限制返回的视频数量，如果为null则返回默认数量
     * @return 返回最新的Video对象列表，按发布时间降序排列
     */
    List<Video> getLatestVideos(Integer limit);

    /**
     * 记录视频观看信息的方法
     * @param videoId 视频ID，用于标识被观看的视频
     * @param viewDuration 观看时长，记录用户观看了多长时间
     * @param request HTTP请求对象，可以从中获取用户IP、设备信息等上下文数据
     */
    void recordVideoView(Long videoId, Integer viewDuration, HttpServletRequest request);

    /**
     * 切换视频点赞状态的方法
     * @param videoId 视频ID，用于标识被点赞的视频
     * @param request HTTP请求对象，可以从中获取用户信息
     * @return 返回一个布尔值，表示点赞操作是否成功
     */
    boolean toggleVideoLike(Long videoId, HttpServletRequest request);

    /**
     * 用户投稿视频的方法
     * @param video 视频信息，包括标题、描述、分类、标签等
     * @param videoFile 视频文件，用于上传视频内容
     * @param coverFile 封面文件，用于上传视频封面
     * @return 返回一个Map，包含投稿结果信息
     */
    Map<String, Object> submitVideo(Video video, MultipartFile videoFile, MultipartFile coverFile);

    /**
     * 获取已发布的视频列表
     *
     * @param page 页码，用于分页查询
     * @param size 每页大小，用于分页查询
     * @param status 状态筛选
     * @param keyword 关键词，用于搜索视频标题或描述
     * @param uploaderType 上传者类型筛选
     * @return 返回一个IPage<Video>对象，包含分页信息和视频数据列表
     */
    IPage<Video> getVideosForAdmin(Integer page, Integer size, Integer status, Integer uploaderType, String keyword);

    /**
     * 管理员上传视频
     * @param video 视频信息
     * @param videoFile 视频文件
     * @param coverFile 封面文件
     * @param adminId 管理员ID
     * @return 上传结果
     */
    Map<String, Object> uploadVideoByAdmin(Video video, MultipartFile videoFile, MultipartFile coverFile, Long adminId);

    /**
     * 审核视频
     * @param videoId 视频ID
     * @param status 审核状态（1-通过，2-拒绝）
     * @param reason 审核原因（拒绝时必填）
     * @param adminId 管理员ID
     * @return 操作结果
     */
    void auditVideo(Long videoId, Integer status, String reason, Long adminId);

    /**
     * 下架视频
     * @param videoId 视频ID
     * @param adminId 管理员ID
     * @return 操作结果
     */
    void offlineVideo(Long videoId, Long adminId);

    /**
     * 删除视频
     * @param videoId 视频ID
     * @return 操作结果
     */
    void deleteVideo(Long videoId);

    /**
     * 获取视频统计数据
     * @return 统计数据
     * */
    Map<String, Object> getVideoStatistics();

    /**
     * 获取视频详细统计数据
     * @param videoId 视频ID
     * @param days 统计天数，默认30天
     * @return 详细统计数据
     * */
    Map<String, Object> getVideoDetailStats(Long videoId, Integer days);
}
