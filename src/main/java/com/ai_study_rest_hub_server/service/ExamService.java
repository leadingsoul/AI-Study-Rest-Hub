package com.ai_study_rest_hub_server.service;

import com.ai_study_rest_hub_server.entity.ExamRecord;
import com.ai_study_rest_hub_server.vo.ExamRankingVO;
import com.ai_study_rest_hub_server.vo.StartExamVo;
import com.ai_study_rest_hub_server.vo.SubmitAnswerVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.InterruptedIOException;
import java.util.List;

/**
 * 考试服务接口
 */
public interface ExamService extends IService<ExamRecord> {

    /**
     * 开始考试的方法
     * @param startExamVo 开始考试的数据传输对象，包含考试开始所需的相关信息
     * @return 返回考试记录对象，包含考试开始后的相关信息
     */
    ExamRecord startExam(StartExamVo startExamVo);

    ExamRecord getExamRecordById(Integer id);

    void submitAnswers(Integer examRecordId, List<SubmitAnswerVo> answers) throws InterruptedIOException;

    void AIGradeExam(Integer examRecordId) throws InterruptedIOException;

    void RemoveById(Integer id);

    List<ExamRankingVO> getExamRanking(Integer paperId, Integer limit);
}
 