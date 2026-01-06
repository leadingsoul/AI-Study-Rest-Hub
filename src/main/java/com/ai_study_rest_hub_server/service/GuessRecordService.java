package com.ai_study_rest_hub_server.service;

import com.ai_study_rest_hub_server.entity.GuessRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author leadingsoul
* @description 针对表【guess_records( 猜词记录表 )】的数据库操作Service
* @createDate 2026-01-01 17:12:28
*/
public interface GuessRecordService extends IService<GuessRecord> {

    /**
     * 保存猜测记录的方法
     * @param guessRecord 包含猜测信息的记录对象
     */
    void saveRecord(GuessRecord guessRecord);
}
