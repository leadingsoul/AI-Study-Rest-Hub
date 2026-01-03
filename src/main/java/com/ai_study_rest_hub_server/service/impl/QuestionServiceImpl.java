package com.ai_study_rest_hub_server.service.impl;

import com.ai_study_rest_hub_server.constant.CacheConstants;
import com.ai_study_rest_hub_server.entity.PaperQuestion;
import com.ai_study_rest_hub_server.entity.QuestionAnswer;
import com.ai_study_rest_hub_server.entity.QuestionChoice;
import com.ai_study_rest_hub_server.mapper.PaperQuestionMapper;
import com.ai_study_rest_hub_server.mapper.QuestionAnswerMapper;
import com.ai_study_rest_hub_server.mapper.QuestionChoiceMapper;
import com.ai_study_rest_hub_server.utils.RedisUtils;
import com.ai_study_rest_hub_server.vo.QuestionQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ai_study_rest_hub_server.entity.Question;
import com.ai_study_rest_hub_server.service.QuestionService;
import com.ai_study_rest_hub_server.mapper.QuestionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
* @author 刘博啸辉
* &#064;description  针对表【questions】的数据库操作Service实现
* &#064;createDate  2026-01-01 17:14:13
 */
@Slf4j
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private QuestionChoiceMapper questionChoiceMapper;
    @Autowired
    private QuestionAnswerMapper questionAnswerMapper;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private PaperQuestionMapper paperQuestionMapper;

    @Override
    public void customPageJavaService(Page<Question> pageBean, QuestionQueryVo questionQueryVo) {
        //分页查询题目列表（多条件)
        LambdaQueryWrapper<Question> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(!ObjectUtils.isEmpty(questionQueryVo.getType()), Question::getType, questionQueryVo.getType());
        lambdaQueryWrapper.eq(!ObjectUtils.isEmpty(questionQueryVo.getDifficulty()), Question::getDifficulty, questionQueryVo.getDifficulty());
        lambdaQueryWrapper.eq(!ObjectUtils.isEmpty(questionQueryVo.getCategoryId()), Question::getCategoryId, questionQueryVo.getCategoryId());
        lambdaQueryWrapper.like(!ObjectUtils.isEmpty(questionQueryVo.getKeyword()), Question::getTitle, questionQueryVo.getKeyword());
        //时间的倒序排序
        lambdaQueryWrapper.orderByDesc(Question::getCreateTime);
        page(pageBean, lambdaQueryWrapper);
        fillQuestionChoiceAndAnswer(pageBean.getRecords());
    }

    @Override
    public Question queryQuestionById(Long id) {
        //1.查询题目详情
        Question question = getById(id);
        if(question== null){
            throw new RuntimeException("查询id为%s的题目不存在".formatted(id));
        }
        //2.查询选项和答案
        QuestionAnswer questionAnswer = questionAnswerMapper.selectOne(new LambdaQueryWrapper<QuestionAnswer>().eq(QuestionAnswer::getQuestionId, id));
        question.setAnswer(questionAnswer);
        if ("CHOICE".equals(question.getType())){
            List<QuestionChoice> questionChoices = questionChoiceMapper.selectList(new LambdaQueryWrapper<QuestionChoice>().eq(QuestionChoice::getQuestionId, id));
            question.setChoices(questionChoices);
        }
        //3.进行redis的数据缓存zset
        new Thread(() -> {
            incrementQuestionScore(question.getId());

        }).start();
        return question;
    }

    @Override
    public void saveQuestion(Question question) {
        //插入题目信息（回显题目id）
        LambdaQueryWrapper<Question> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Question::getTitle, question.getTitle());
        lambdaQueryWrapper.eq(Question::getType, question.getType());
        boolean exists = baseMapper.exists(lambdaQueryWrapper);
        if(exists){
            throw new RuntimeException("在%s下，存在%s名称题目已存在！保存失败".formatted(question.getType(),question.getTitle()));
        }
        boolean saved = save(question);
        if(!saved){
            //同一个类型，title相同
            throw new RuntimeException("在%s下，存在%s名称题目保存失败".formatted(question.getType(),question.getTitle()));
        }
        QuestionAnswer answer = question.getAnswer();
        answer.setQuestionId(question.getId());
        //判断是否为选择题
        if ("CHOICE".equals(question.getType())){
            List<QuestionChoice> choices = question.getChoices();
            StringBuilder sb = new StringBuilder();
            for(int i=0;i<choices.size();i++){
                QuestionChoice choice = choices.get(i);
                choice.setSort(i);
                choice.setQuestionId(question.getId());
                questionChoiceMapper.insert(choice);
                if(choice.getIsCorrect()){
                    if(sb.length()>0){
                        sb.append(",");
                    }
                    sb.append((char)('A'+i));
                }
            }
            answer.setAnswer(sb.toString());
        }
        questionAnswerMapper.insert(answer);
    }

    @Override
    public void updateQuestion(Question question) {
        //1.校验题目
        LambdaQueryWrapper<Question> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Question::getTitle, question.getTitle());
        lambdaQueryWrapper.eq(Question::getId, question.getId());
        boolean exists = baseMapper.exists(lambdaQueryWrapper);
        if(exists){
            throw new RuntimeException("修改：%s题目的新标题：%s和其他的题目重复了！修改失败了！".formatted(question.getId(),question.getTitle()));
        }
        //2.更新题目
        boolean update = updateById(question);
        if(!update){
            throw new RuntimeException("修改：%s题目失败！".formatted(question.getId()));
        }
        //3.获取答案对象
        QuestionAnswer answer = question.getAnswer();
        //4.判断是选择题
        if ("CHOICE".equals(question.getType())){
            List<QuestionChoice> choices = question.getChoices();
            LambdaQueryWrapper<QuestionChoice> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
            lambdaQueryWrapper1.eq(QuestionChoice::getQuestionId, question.getId());
            questionChoiceMapper.delete(lambdaQueryWrapper1);
            //拼接正确答案
            StringBuilder sb = new StringBuilder();
            for(int i=0;i<choices.size();i++){
                QuestionChoice choice = choices.get(i);
                choice.setId(null);
                choice.setSort(i);
                choice.setCreateTime(null);
                choice.setUpdateTime(null);
                choice.setQuestionId(question.getId());
                questionChoiceMapper.insert(choice);
                if(choice.getIsCorrect()){
                    if(sb.length()>0){
                        sb.append(",");
                    }
                    sb.append((char)('A'+i));
                }
            }
            //答案对象赋值
            answer.setAnswer(sb.toString());
        }
        questionAnswerMapper.updateById(answer);
    }

    @Override
    public void deleteQuestion(Long id) {
        //1.校验题目
        LambdaQueryWrapper<PaperQuestion> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(PaperQuestion::getQuestionId, id);
        Long count = paperQuestionMapper.selectCount(lambdaQueryWrapper);
        if(count>0){
            throw new RuntimeException("该题目:%s已被试卷引用了%s次，无法删除！".formatted(id,count));
        }
        //2.删除题目
        boolean removed = removeById(id);
        if(!removed){
            throw new RuntimeException("删除题目:%s 失败！".formatted(id));
        }
        //3.删除答案
        questionAnswerMapper.delete(new LambdaQueryWrapper<QuestionAnswer>().eq(QuestionAnswer::getQuestionId, id));
        questionChoiceMapper.delete(new LambdaQueryWrapper<QuestionChoice>().eq(QuestionChoice::getQuestionId, id));
    }

    @Override
    public List<Question> getPopularQuestions(Integer size) {
        //定义列表
        List<Question> questions = new ArrayList<>();
        //从缓存中获取数据
        Set<Object> popularIds = redisUtils.zReverseRange(CacheConstants.POPULAR_QUESTIONS_KEY,0,size-1);
        //定义接收id的集合
        List<Long> ids = popularIds.stream().map(id->Long.valueOf(id.toString())).collect(Collectors.toList());
        //处理热门题目
        //List<Question> questions = listByIds(ids);
        for(Long id:ids){
            Question question = getById(id);
            if(question!=null){
                questions.add(question);
            }
        }
        int diff = size - questions.size();
        if(diff>0){
            LambdaQueryWrapper<Question> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.orderByDesc(Question::getCreateTime);
            List<Long> existQuestionId = questions.stream().map(Question::getId).collect(Collectors.toList());
            lambdaQueryWrapper.notIn(!ObjectUtils.isEmpty(existQuestionId),Question::getId,existQuestionId);
            //切割指定的diff条
            lambdaQueryWrapper.last("limit "+diff);
            List<Question> questionList = list(lambdaQueryWrapper);
            questions.addAll(questionList);
        }
        fillQuestionChoiceAndAnswer(questions);
        return questions;
    }

    @Override
    public List<Question> getQuestionsByCategoryId(Long categoryId) {
        LambdaQueryWrapper<Question> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Question::getCategoryId, categoryId);
        List<Question> questions = list(lambdaQueryWrapper);
        fillQuestionChoiceAndAnswer(questions);
        return questions;
    }

    @Override
    public List<Question> getQuestionsByDifficulty(String difficulty) {
        LambdaQueryWrapper<Question> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Question::getDifficulty, difficulty);
        List<Question> questions = list(lambdaQueryWrapper);
        fillQuestionChoiceAndAnswer(questions);
        return questions;
    }

    private void fillQuestionChoiceAndAnswer(List<Question> questions) {
        //2.提取一个方法，用于装填选项和答案
        if (questions == null || questions.isEmpty()) {
            log.debug("没有查询对应的问题集合数据！！");
            return;
        }
        List<Long> ids = questions.stream().map(Question::getId).collect(Collectors.toList());

        LambdaQueryWrapper<QuestionChoice> choiceLambdaQueryWrapper = new LambdaQueryWrapper<>();
        choiceLambdaQueryWrapper.in(QuestionChoice::getQuestionId, ids);
        List<QuestionChoice> questionChoiceList = questionChoiceMapper.selectList(choiceLambdaQueryWrapper);

        LambdaQueryWrapper<QuestionAnswer> answerLambdaQueryWrapper = new LambdaQueryWrapper<>();
        answerLambdaQueryWrapper.in(QuestionAnswer::getQuestionId, ids);
        List<QuestionAnswer> questionAnswerList = questionAnswerMapper.selectList(answerLambdaQueryWrapper);
        //3.将选项和答案装填到题目中
        Map<Long, List<QuestionChoice>> choiceMap = questionChoiceList.stream().collect(Collectors.groupingBy(QuestionChoice::getQuestionId));
        Map<Long, QuestionAnswer> answerMap = questionAnswerList.stream().collect(Collectors.toMap(QuestionAnswer::getQuestionId, questionAnswer -> questionAnswer));
        //4.赋值
        questions.forEach(question -> {
            //选择题才有选项
            if ("CHOICE".equals(question.getType())){
                List<QuestionChoice> questionChoices = choiceMap.get(question.getId());
                questionChoices.sort(Comparator.comparingInt(QuestionChoice::getSort));
                question.setChoices(questionChoices);
            }
            question.setAnswer(answerMap.get(question.getId()));

        });
    }

    /**
     * 增加问题得分的方法
     * @param id 问题的唯一标识符，用于指定需要增加得分的问题
     */
    private void incrementQuestionScore(Long id) {
        Double score = redisUtils.zIncrementScore(CacheConstants.POPULAR_QUESTIONS_KEY, id, 1);
        log.info("问题ID为{}的得分增加成功，当前得分为{}", id, score);
    }


}




