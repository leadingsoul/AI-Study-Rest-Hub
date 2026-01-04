package com.ai_study_rest_hub_server.mapper;

import com.ai_study_rest_hub_server.entity.ExamRecord;
import com.ai_study_rest_hub_server.vo.ExamRankingVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author leadingsoul
* @description 针对表【exam_records】的数据库操作Mapper
* @createDate 2026-01-01 17:11:32
* @Entity com.ai_study_rest_hub_server.entity.ExamRecords
*/
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {

    List<ExamRankingVO> queryRanking(Integer paperId, Integer limit);
}




