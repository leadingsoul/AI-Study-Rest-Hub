package com.ai_study_rest_hub_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ai_study_rest_hub_server.entity.Notice;
import com.ai_study_rest_hub_server.service.NoticeService;
import com.ai_study_rest_hub_server.mapper.NoticeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 刘博啸辉
* @description 针对表【notices(公告表)】的数据库操作Service实现
* @createDate 2026-01-01 17:12:54
*/
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice>
    implements NoticeService {

    @Override
    public List<Notice> getActiveNotices() {
        LambdaQueryWrapper<Notice> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Notice::getIsActive , true);
        lambdaQueryWrapper.orderByAsc(Notice::getPriority);
        return list(lambdaQueryWrapper);
    }

    @Override
    public List<Notice> getLatestNotices(int limit) {
        LambdaQueryWrapper<Notice> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Notice::getIsActive , true);
        lambdaQueryWrapper.orderByDesc(Notice::getCreateTime);
        lambdaQueryWrapper.orderByAsc(Notice::getPriority);
        List<Notice> noticeList = list(lambdaQueryWrapper);
        int count = noticeList.size();
        if (count > limit) {
            return noticeList.subList(0, limit);
        } else {
            return noticeList;
        }
    }

    @Override
    public List<Notice> getAllNotices() {
        LambdaQueryWrapper<Notice> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        return list(lambdaQueryWrapper);
    }

    @Override
    public String addNotice(Notice notice) {
        if(!notice.getIsActive()){
            notice.setIsActive(true);
        }
        if(notice.getPriority()==null){
            notice.setPriority(0);
        }
        boolean saveFlag = save(notice);
        if (saveFlag) {
            return "公告添加成功";
        } else {
            return "公告添加失败";
        }
    }

    @Override
    public String updateNotice(Notice notice) {
        boolean updateFlag = updateById(notice);
        if (updateFlag) {
            return "公告更新成功";
        } else {
            return "公告更新失败";
        }
    }

    @Override
    public String deleteNotice(Long id) {
        boolean deleteFlag = removeById(id);
        if (deleteFlag) {
            return "公告删除成功";
        } else {
            return "公告删除失败";
        }
    }

    @Override
    public String toggleNoticeStatus(Long id, Boolean isActive) {
        LambdaUpdateWrapper<Notice> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Notice::getId, id);
        lambdaUpdateWrapper.set(Notice::getIsActive, isActive);
        boolean updateFlag = update(lambdaUpdateWrapper);
        if (updateFlag) {
            return "公告状态更新成功";
        } else {
            return "公告状态更新失败";
        }
    }
}
