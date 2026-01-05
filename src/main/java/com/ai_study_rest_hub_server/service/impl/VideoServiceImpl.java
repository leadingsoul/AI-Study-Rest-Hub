package com.ai_study_rest_hub_server.service.impl;

import com.ai_study_rest_hub_server.entity.VideoCategory;
import com.ai_study_rest_hub_server.entity.VideoLike;
import com.ai_study_rest_hub_server.entity.VideoView;
import com.ai_study_rest_hub_server.mapper.VideoCategoryMapper;
import com.ai_study_rest_hub_server.mapper.VideoLikeMapper;
import com.ai_study_rest_hub_server.mapper.VideoViewMapper;
import com.ai_study_rest_hub_server.service.FileUploadService;
import com.ai_study_rest_hub_server.utils.IpUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ai_study_rest_hub_server.entity.Video;
import com.ai_study_rest_hub_server.service.VideoService;
import com.ai_study_rest_hub_server.mapper.VideoMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author 刘博啸辉
* @description 针对表【videos(视频信息表)】的数据库操作Service实现
* @createDate 2026-01-01 17:14:54
*/
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video>
    implements VideoService {

    private final VideoMapper videoMapper;
    private final VideoLikeMapper videoLikeMapper;
    private final VideoCategoryMapper videoCategoryMapper;
    private final VideoViewMapper videoViewMapper;
    private final FileUploadService fileUploadService;

    @Override
    public IPage<Video> getPublishedVideos(Integer page, Integer size, Long categoryId, String keyword, HttpServletRequest request) {
        Page<Video> pageObj = new Page<>(page,size);
        IPage<Video> videoPage = videoMapper.getPublishedVideos(pageObj,categoryId,keyword);
        //如果有Ip，填充点赞状态
        if(request!=null){
            String userIp = IpUtils.getClientIp(request);
            videoPage.getRecords().forEach(video -> {
                boolean isLiked = videoLikeMapper.isLikedById(video.getId(),userIp);
                video.setIsLike(isLiked);
                //格式化视频信息
                formatVideoInfo(video);
            });
        }
        return videoPage;
    }

    @Override
    public Video getVideoDetail(Long id, HttpServletRequest request) {
        Video video = videoMapper.selectById(id);
        if(video==null){
            throw new RuntimeException("视频不存在");
        }
        //只有已发布的视频才能查看详情
        if(video.getStatus()!=Video.STATUS_PUBLISHED){
            throw new RuntimeException("视频未发布或已下架");
        }
        //获取分类名称并赋值
        if(video.getCategoryId()!=null){
            VideoCategory category = videoCategoryMapper.selectById(video.getCategoryId());//查询分类信息
            if(category!=null){
                video.setCategoryName(category.getName());//设置分类名称
            }
            else {
                video.setCategoryName("未知分类");
            }
        }
        else {
            video.setCategoryName("未知分类");
        }
        //如果有IP，填充点赞状态
        if(request!=null) {
            String userIp = IpUtils.getClientIp(request);
            boolean isLiked = videoLikeMapper.isLikedById(video.getId(), userIp);
            video.setIsLike(isLiked);
        }
        //格式化视频信息
        formatVideoInfo(video);
        return video;
    }

    @Override
    public List<Video> getPopularVideos(Integer limit) {
        List<Video> videos = videoMapper.getPopularVideos(limit);
        videos.forEach(this::formatVideoInfo);
        return videos;
    }

    @Override
    public List<Video> getLatestVideos(Integer limit) {
        List<Video> videos = videoMapper.getLatestVideos(limit);
        videos.forEach(this::formatVideoInfo);
        return videos;
    }

    @Override
    public void recordVideoView(Long videoId, Integer viewDuration, HttpServletRequest request) {
        //如果ip是无，返回，说明没有观看
        if(request==null) {
            return;
        }
        String userIp = IpUtils.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        //创建观看记录
        VideoView videoView = new VideoView();
        videoView.setVideoId(videoId);
        videoView.setUserIp(userIp);
        videoView.setUserAgent(userAgent);
        videoView.setViewDuration(viewDuration);
        videoView.setCreatedAt(LocalDateTime.now());

        videoViewMapper.insert(videoView);
        videoMapper.incrementViewCount(videoId);
    }

    @Override
    public boolean toggleVideoLike(Long videoId, HttpServletRequest request) {
        //检查IP是否为空
        if(request==null){
            throw new RuntimeException("请求信息为空");
        }
        String userIp = IpUtils.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        //检查是否已经点赞
        boolean isLiked = videoLikeMapper.isLikedById(videoId,userIp);
        if(isLiked){
            videoLikeMapper.delete(new LambdaQueryWrapper<VideoLike>().eq(VideoLike::getVideoId,videoId).eq(VideoLike::getUserIp,userIp));
            videoMapper.decrementLikeCount(videoId);
            return false;
        }
        else {
            VideoLike videoLike = new VideoLike();
            videoLike.setVideoId(videoId);
            videoLike.setUserIp(userIp);
            videoLike.setUserAgent(userAgent);
            videoLike.setCreatedAt(LocalDateTime.now());
            videoLikeMapper.insert(videoLike);
            videoMapper.incrementLikeCount(videoId);
            return true;
        }
    }

    @Override
    public Map<String, Object> submitVideo(Video video, MultipartFile videoFile, MultipartFile coverFile) {
        Map<String, Object> result = new HashMap<>();

        if (videoFile == null || videoFile.isEmpty()) {
            throw new RuntimeException("视频文件不能为空");
        }

        try {
            // 上传视频文件
            String videoUrl = fileUploadService.uploadFile(videoFile, "videos/original/");
            video.setFileUrl(videoUrl);
            video.setFileSize(videoFile.getSize());

            // 上传封面文件（可选）
            if (coverFile != null && !coverFile.isEmpty()) {
               String coverUrl = fileUploadService.uploadFile(coverFile, "videos/covers/");
                video.setCoverUrl(coverUrl);
            }

            // 设置用户投稿默认值
            video.setUploaderType(Video.UPLOADER_TYPE_USER);
            video.setStatus(Video.STATUS_PENDING); // 待审核
            video.setViewCount(0L);
            video.setLikeCount(0L);
            video.setCreatedAt(LocalDateTime.now());
            video.setUpdatedAt(LocalDateTime.now());

            // 保存视频信息
            videoMapper.insert(video);

            result.put("success", true);
            result.put("message", "视频投稿成功，请等待审核");
            result.put("videoId", video.getId());

        } catch (Exception e) {
            throw new RuntimeException("视频上传失败：" + e.getMessage());
        }

        return result;

    }

    @Override
    public IPage<Video> getVideosForAdmin(Integer page, Integer size, Integer status, Integer uploaderType, String keyword) {
        Page<Video> pageObj = new Page<>(page,size);
        IPage<Video> videoPage = videoMapper.getVideosForAdmin(pageObj,status,uploaderType,keyword);
        //格式化视频信息
        videoPage.getRecords().forEach(video -> {
            formatVideoInfo(video);
            formatVideoStatus(video);
        });
        return videoPage;
    }

    @Override
    public Map<String, Object> uploadVideoByAdmin(Video video, MultipartFile videoFile, MultipartFile coverFile, Long adminId) {
        Map<String, Object> result = new HashMap<>();
        if (videoFile == null || videoFile.isEmpty()) {
            throw new RuntimeException("视频文件不能为空");
        }
        try {
            // 上传视频文件
            String videoUrl = fileUploadService.uploadFile(videoFile, "videos/original/");
            video.setFileUrl(videoUrl);
            video.setFileSize(videoFile.getSize());

            // 上传封面文件
            if (coverFile != null && !coverFile.isEmpty()) {
                String coverUrl = fileUploadService.uploadFile(coverFile, "videos/covers/");
                video.setCoverUrl(coverUrl);
            }

            // 设置管理员投稿默认值
            video.setUploaderType(Video.UPLOADER_TYPE_ADMIN);
            video.setAdminId(adminId);
            video.setStatus(Video.STATUS_PUBLISHED); // 直接发布
            video.setAuditAdminId(adminId);
            video.setAuditTime(LocalDateTime.now());
            video.setViewCount(0L);
            video.setLikeCount(0L);
            video.setCreatedAt(LocalDateTime.now());
            video.setUpdatedAt(LocalDateTime.now());

            // 保存视频信息
            videoMapper.insert(video);

            result.put("success", true);
            result.put("message", "视频上传成功");
            result.put("videoId", video.getId());

        } catch (Exception e) {
            throw new RuntimeException("视频上传失败：" + e.getMessage());
        }
        return result;
    }

    @Override
    public void auditVideo(Long videoId, Integer status, String reason, Long adminId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new RuntimeException("视频不存在");
        }
        if(video.getStatus() != Video.STATUS_PENDING){
            throw new RuntimeException("只能审核待审核状态的视频");
        }
        if(status== Video.STATUS_REJECTED && (reason==null||reason.trim().isEmpty())){
            throw new RuntimeException("拒绝时必须填写原因");
        }
        video.setStatus(status);
        video.setAuditAdminId(adminId);
        video.setAuditTime(LocalDateTime.now());
        video.setAuditReason(reason);
        video.setUpdatedAt(LocalDateTime.now());
        videoMapper.updateById(video);
    }

    @Override
    public void offlineVideo(Long videoId, Long adminId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new RuntimeException("视频不存在");
        }
        if(video.getStatus() != Video.STATUS_PUBLISHED){
            throw new RuntimeException("只能下架已发布的视频");
        }
        video.setStatus(Video.STATUS_OFFLINE);
        video.setAuditAdminId(adminId);
        video.setAuditTime(LocalDateTime.now());
        video.setUpdatedAt(LocalDateTime.now());
        videoMapper.updateById(video);
    }

    @Override
    public void deleteVideo(Long videoId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new RuntimeException("视频不存在");
        }

        // 删除相关数据
        videoLikeMapper.delete(new LambdaQueryWrapper<VideoLike>().eq(VideoLike::getVideoId, videoId));
        videoViewMapper.delete(new LambdaQueryWrapper<VideoView>().eq(VideoView::getVideoId, videoId));

        // 删除视频记录
        videoMapper.deleteById(videoId);

        // 删除视频文件和封面文件
        boolean isFileUrlDeleted = fileUploadService.deleteFile(video.getFileUrl());
        if (!isFileUrlDeleted) {
            log.warn("视频文件删除失败: {}", video.getFileUrl());
        }
        else {
            log.info("视频文件删除成功: {}", video.getFileUrl());
        }
        boolean isCoverUrlDeleted = fileUploadService.deleteFile(video.getCoverUrl());
        if (!isCoverUrlDeleted) {
            log.warn("封面文件删除失败: {}", video.getCoverUrl());
        }
        else {
            log.info("封面文件删除成功: {}", video.getCoverUrl());
        }
    }

    @Override
    public Map<String, Object> getVideoStatistics() {
        return videoMapper.getVideoStatistics();
    }

    @Override
    public Map<String, Object> getVideoDetailStats(Long videoId, Integer days) {
        Map<String, Object> stats = new HashMap<>();

        // 基本统计
        Long viewCount = videoViewMapper.getViewCountByVideoId(videoId);
        Long likeCount = videoLikeMapper.getLikeCountByVideoId(videoId);
        Double avgDuration = videoViewMapper.getAverageViewDuration(videoId);

        stats.put("viewCount", viewCount);
        stats.put("likeCount", likeCount);
        stats.put("averageViewDuration", avgDuration);

        // 按日期统计
        List<Map<String, Object>> dailyStats = videoViewMapper.getViewStatsByDate(videoId, days);
        stats.put("dailyViewStats", dailyStats);

        return stats;
    }

    private void formatVideoStatus(Video video) {
        // 上传者类型文本
        if (video.getUploaderType() == Video.UPLOADER_TYPE_USER) {
            video.setUploaderTypeText("用户投稿");
        } else if (video.getUploaderType() == Video.UPLOADER_TYPE_ADMIN) {
            video.setUploaderTypeText("管理员上传");
        }
        // 状态文本
        switch (video.getStatus()) {
            case 0:
                video.setStatusText("待审核");
                break;
            case 1:
                video.setStatusText("已发布");
                break;
            case 2:
                video.setStatusText("已拒绝");
                break;
            case 3:
                video.setStatusText("已下架");
                break;
            default:
                video.setStatusText("未知状态");
        }
    }


    //格式化视频信息
    private void formatVideoInfo(Video video) {
        //格式化时长
        if(video.getDuration()!=null){
            int minutes = video.getDuration() / 60;
            int seconds = video.getDuration() % 60;
            video.setDurationText(String.format("%02d:%02d", minutes, seconds));
        }
        //格式化文件大小
        if(video.getFileSize()!=null){
            video.setFileSizeText(formatFileSize(video.getFileSize()));
        }
    }

    //格式化文件大小
    private String formatFileSize(Long fileSize) {
        if (fileSize < 1024) {
            return fileSize + "B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1fKB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1fMB", fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.1fGB", fileSize / (1024.0 * 1024 * 1024));
        }
    }
}




