package com.ai_study_rest_hub_server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 *  猜词题目表 
 * @TableName guess_topics
 */
@TableName(value ="guess_topics")
@Data
public class GuessTopic extends BaseEntity{

    /**
     *  猜词题目名称 
     */
    private String topicName;

    /**
     *  猜词题目描述 
     */
    private String topicDescription;

    /**
     *  猜词分类 ID
     */
    private Long categoryId;

    /**
     *  猜词记录表统计 (用户已参与次数)
     */
    private Long recordCount;

    /**
     *  猜词难度 EASY/MEDIUM/HARD
     */
    private String difficulty;

    /**
     *  目标词 
     */
    private String target;

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
        GuessTopic other = (GuessTopic) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTopicName() == null ? other.getTopicName() == null : this.getTopicName().equals(other.getTopicName()))
            && (this.getTopicDescription() == null ? other.getTopicDescription() == null : this.getTopicDescription().equals(other.getTopicDescription()))
            && (this.getCategoryId() == null ? other.getCategoryId() == null : this.getCategoryId().equals(other.getCategoryId()))
            && (this.getRecordCount() == null ? other.getRecordCount() == null : this.getRecordCount().equals(other.getRecordCount()))
            && (this.getDifficulty() == null ? other.getDifficulty() == null : this.getDifficulty().equals(other.getDifficulty()))
            && (this.getTarget() == null ? other.getTarget() == null : this.getTarget().equals(other.getTarget()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getIsDeleted() == null ? other.getIsDeleted() == null : this.getIsDeleted().equals(other.getIsDeleted()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTopicName() == null) ? 0 : getTopicName().hashCode());
        result = prime * result + ((getTopicDescription() == null) ? 0 : getTopicDescription().hashCode());
        result = prime * result + ((getCategoryId() == null) ? 0 : getCategoryId().hashCode());
        result = prime * result + ((getRecordCount() == null) ? 0 : getRecordCount().hashCode());
        result = prime * result + ((getDifficulty() == null) ? 0 : getDifficulty().hashCode());
        result = prime * result + ((getTarget() == null) ? 0 : getTarget().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getIsDeleted() == null) ? 0 : getIsDeleted().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", topicName=").append(topicName);
        sb.append(", topicDescription=").append(topicDescription);
        sb.append(", categoryId=").append(categoryId);
        sb.append(", recordCount=").append(recordCount);
        sb.append(", difficulty=").append(difficulty);
        sb.append(", target=").append(target);
        sb.append("]");
        return sb.toString();
    }
}