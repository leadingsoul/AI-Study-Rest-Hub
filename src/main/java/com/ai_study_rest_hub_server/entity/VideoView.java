package com.ai_study_rest_hub_server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 视频观看记录表
 * @TableName video_views
 */
@TableName(value ="video_views")
@Data
public class VideoView{
    /**
     * 观看ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 视频ID
     */
    private Long videoId;

    /**
     * 用户IP
     */
    private String userIp;

    /**
     * 用户代理信息
     */
    private String userAgent;

    /**
     * 观看时长（秒）
     */
    private Integer viewDuration;

    /**
     * 观看时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String videoTitle;

    @TableField(exist = false)
    private String viewDurationText;

    @TableField(exist = false)
    private Double completionRate;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        VideoView other = (VideoView) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getVideoId() == null ? other.getVideoId() == null : this.getVideoId().equals(other.getVideoId()))
            && (this.getUserIp() == null ? other.getUserIp() == null : this.getUserIp().equals(other.getUserIp()))
            && (this.getUserAgent() == null ? other.getUserAgent() == null : this.getUserAgent().equals(other.getUserAgent()))
            && (this.getViewDuration() == null ? other.getViewDuration() == null : this.getViewDuration().equals(other.getViewDuration()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getVideoId() == null) ? 0 : getVideoId().hashCode());
        result = prime * result + ((getUserIp() == null) ? 0 : getUserIp().hashCode());
        result = prime * result + ((getUserAgent() == null) ? 0 : getUserAgent().hashCode());
        result = prime * result + ((getViewDuration() == null) ? 0 : getViewDuration().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", videoId=").append(videoId);
        sb.append(", userIp=").append(userIp);
        sb.append(", userAgent=").append(userAgent);
        sb.append(", viewDuration=").append(viewDuration);
        sb.append(", createdAt=").append(createdAt);
        sb.append("]");
        return sb.toString();
    }
}