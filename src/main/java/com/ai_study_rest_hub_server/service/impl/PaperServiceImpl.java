package com.ai_study_rest_hub_server.service.impl;

import com.ai_study_rest_hub_server.entity.ExamRecord;
import com.ai_study_rest_hub_server.entity.PaperQuestion;
import com.ai_study_rest_hub_server.entity.Question;
import com.ai_study_rest_hub_server.mapper.ExamRecordMapper;
import com.ai_study_rest_hub_server.mapper.PaperQuestionMapper;
import com.ai_study_rest_hub_server.mapper.QuestionMapper;
import com.ai_study_rest_hub_server.service.PaperQuestionService;
import com.ai_study_rest_hub_server.vo.AiPaperVo;
import com.ai_study_rest_hub_server.vo.PaperVo;
import com.ai_study_rest_hub_server.vo.RuleVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ai_study_rest_hub_server.entity.Paper;
import com.ai_study_rest_hub_server.service.PaperService;
import com.ai_study_rest_hub_server.mapper.PaperMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
* @author 刘博啸辉
* @description 针对表【paper】的数据库操作Service实现
* @createDate 2026-01-01 17:13:09
*/
@RequiredArgsConstructor
@Service
@Slf4j
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper>
    implements PaperService{

    private final PaperQuestionService paperQuestionService;
    private final QuestionMapper questionMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final ExamRecordMapper examRecordMapper;

    @Override
    public Paper getDetailById(Integer id) {
        //1.单表java代码进行paper查询
        Paper paper = getById(id);
        //2.校验paper=null
        if (paper == null) {
            throw new RuntimeException("id:%s试卷不存在".formatted(id));
        }
        //3.根据paperid查询题目集合
        List<Question> questionList = questionMapper.queryQuestionListByPageId(id);
        //4.校验题目集合=null
        if(ObjectUtils.isEmpty(questionList)){
            paper.setQuestions(new ArrayList<>());
            log.warn("试卷中没有题目！可以进行试卷编辑！但是不能用于考试！！,对应试卷id：{}",id);
            return paper;
        }
        //5.对题目集合进行排序
        questionList.sort((o1, o2) -> Integer.compare(typeToInt(o1.getType()), typeToInt(o2.getType())));
        //选择(1)>判断(2)>简答(3)
        paper.setQuestions(questionList);
        return paper;
    }

    @Transactional
    @Override
    public Paper createPaper(PaperVo paperVo) {
        //1.完善试卷内信息 名字 描述 时间->状态，总题目数，总分数
        Paper paper = new Paper();
        BeanUtils.copyProperties(paperVo, paper);
        paper.setStatus("DRAFT");
        if(ObjectUtils.isEmpty(paperVo.getQuestions())){
            //本次没选题目
            paper.setTotalScore(BigDecimal.ZERO);
            paper.setQuestionCount(0);
            save(paper);
            log.warn("本次{}组卷，没有选择题目！注意没有题目的试卷无法进行考试！！",paper);
            return paper;
        }
        //2.完善试卷内信息
        paper.setQuestionCount(paperVo.getQuestions().size());
        paper.setTotalScore(paperVo.getQuestions().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
        save(paper);
        //3.给中间表集合插入【批量插入】
        List<PaperQuestion> paperQuestionList = paperVo.getQuestions().entrySet().stream()
                .map(entry -> new PaperQuestion(paper.getId().intValue(), entry.getKey().longValue(), entry.getValue()))
                .collect(Collectors.toList());
        //4.中间表的批量插入
        paperQuestionService.saveBatch(paperQuestionList);
        return paper;
    }

    @Override
    public Paper AICreatePaper(AiPaperVo aiPaperVo) {
        //1.完善试卷内信息 名字 描述 时间->状态，总题目数，总分数
        Paper paper = new Paper();
        BeanUtils.copyProperties(aiPaperVo, paper);
        paper.setStatus("DRAFT");
        save(paper);
        //2.组卷规则下的试题选择和中间表的保存
        int questionCount = 0;
        BigDecimal totalScore = BigDecimal.ZERO;
        for(RuleVo rule : aiPaperVo.getRules()){
            //step1 校验规则下的题目数量为0，即跳过
            if(rule.getCount()==0){
                log.warn("在：{}类型下，不需要出题",rule.getType().name());
                continue;
            }
            //step2 根据规则查询题目集合
            LambdaQueryWrapper<Question> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(Question::getType,rule.getType().name());
            lambdaQueryWrapper.in(!ObjectUtils.isEmpty(rule.getCategoryIds()),Question::getCategoryId, rule.getCategoryIds());
            List<Question> allQuestionList = questionMapper.selectList(lambdaQueryWrapper);
            //step3 校验查询的题目集合，若为空，则跳过
            if(ObjectUtils.isEmpty(allQuestionList)){
                log.warn("在：{}类型下，我们指定的分类：{},没有查询到题目信息！",rule.getType().name(),rule.getCategoryIds());
                continue;
            }
            //step4 判断是否有规则下的count数量！没有要全部了
            int realNumber = Math.min(rule.getCount(),allQuestionList.size());
            //step5 本次规则下添加的数量和分数累加
            questionCount += realNumber;
            totalScore =  totalScore.add(BigDecimal.valueOf((long) realNumber * rule.getScore()));
            //step6 随机抽取题目
            Collections.shuffle(allQuestionList);
            List<Question> realQuestionList = allQuestionList.subList(0, realNumber);
            //转为中间表
            List<PaperQuestion> paperQuestionList = realQuestionList.stream().map(
                    question -> new PaperQuestion(paper.getId().intValue(), question.getId(), BigDecimal.valueOf(rule.getScore()))
            ).collect(Collectors.toList());
            paperQuestionService.saveBatch(paperQuestionList);
        }
        //3.修改试卷信息
        paper.setQuestionCount(questionCount);
        paper.setTotalScore(totalScore);
        updateById(paper);
        return paper;
    }

    @Override
    public Paper updatePaper(Integer id, PaperVo paperVo) {
        //1.校验（不能发布状态，不能不同id，name相同）
        Paper paper = getById(id);
        if("PUBLISHED".equals(paper.getStatus())){
            throw new RuntimeException("试卷已发布，不能修改！");
        }
        LambdaQueryWrapper<Paper> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.ne(Paper::getId,id);
        lambdaQueryWrapper.eq(Paper::getName,paperVo.getName());
        long count = count(lambdaQueryWrapper);
        if(count>0){
            throw new RuntimeException("试卷名称%s不能重复！".formatted(paperVo.getName()));
        }
        //2.修改
        BeanUtils.copyProperties(paperVo, paper);
        paper.setQuestionCount(paperVo.getQuestions().size());

        paper.setTotalScore(paperVo.getQuestions().values().stream()
                        .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
        updateById(paper);
        //3.删除中间表，重新插入
        paperQuestionService.remove(new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, paper.getId()));
        List<PaperQuestion> paperQuestionList = paperVo.getQuestions().entrySet().stream()
                .map(entry -> new PaperQuestion(paper.getId().intValue(), entry.getKey().longValue(), entry.getValue()))
                .toList();
        paperQuestionService.saveBatch(paperQuestionList);
        return paper;
    }

    @Override
    public void updatePaperStatus(Integer id, String status) {
        Paper paper = getById(id);
        paper.setStatus(status);
        updateById(paper);
    }

    @Override
    public void deletePaper(Integer id) {
        //1.不是发布状态
        Paper paper = getById(id);
        if (paper == null || "PUBLISHED".equals(paper.getStatus())){
            throw new RuntimeException("发布状态的试卷不能删除！");
        }
        //2.不能有关联的考试记录
        LambdaQueryWrapper<ExamRecord> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ExamRecord::getExamId,id);
        Long count = examRecordMapper.selectCount(lambdaQueryWrapper);
        if (count > 0){
            throw new RuntimeException("当前试卷：%s 下面有关联 %s条考试记录！无法直接删除！".formatted(id,count));
        }
        //3.删除自身表
        removeById(Long.valueOf(id));
        //4.删除中间表
        paperQuestionService.remove(new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId,id));
    }

    public int typeToInt(String type) {
        if (type == null) {
            return 4; // 返回默认值
        }

        switch (type) {
            case "CHOICE": return 1; // 选择题
            case "JUDGE": return 2;  // 判断题
            case "TEXT": return 3;   // 简答题
            default: return 4;       // 其他类型
        }
    }
}




