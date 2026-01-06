package com.ai_study_rest_hub_server.service.impl;

import com.ai_study_rest_hub_server.entity.GuessTopic;
import com.ai_study_rest_hub_server.entity.User;
import com.ai_study_rest_hub_server.mapper.GuessTopicMapper;
import com.ai_study_rest_hub_server.mapper.UserMapper;
import com.ai_study_rest_hub_server.service.GuessTopicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ai_study_rest_hub_server.entity.GuessRecord;
import com.ai_study_rest_hub_server.service.GuessRecordService;
import com.ai_study_rest_hub_server.mapper.GuessRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
* @author 刘博啸辉
* @description 针对表【guess_records( 猜词记录表 )】的数据库操作Service实现
* @createDate 2026-01-01 17:12:28
*/
@Slf4j
@RequiredArgsConstructor
@Service
public class GuessRecordServiceImpl extends ServiceImpl<GuessRecordMapper, GuessRecord>
    implements GuessRecordService {

    private final GuessTopicMapper guessTopicMapper;
    private final UserMapper userMapper;
    private final GuessRecordMapper guessRecordMapper;

    @Override
    public void saveRecord(GuessRecord guessRecord) {
        GuessTopic guessTopic = guessTopicMapper.selectById(guessRecord.getTopicId());
        if(guessTopic == null){
            throw new RuntimeException("题目不存在");
        }
        User user = userMapper.selectById(guessRecord.getUserId());
        if(user == null){
            throw new RuntimeException("用户不存在");
        }
        GuessRecord existingRecord = guessRecordMapper.selectById(guessRecord.getId());
        if(existingRecord != null){
            existingRecord.setGuessCount(existingRecord.getGuessCount() + guessRecord.getGuessCount());
            existingRecord.setSuccessCount(existingRecord.getSuccessCount() + guessRecord.getSuccessCount());
            existingRecord.setDuration(existingRecord.getDuration() + guessRecord.getDuration());
            existingRecord.setStatus(guessRecord.getStatus());
            guessRecordMapper.updateById(existingRecord);
        }
        else{
            guessRecordMapper.insert(guessRecord);
        }
    }
}




