package com.ai_study_rest_hub_server.service;

import com.ai_study_rest_hub_server.entity.Paper;
import com.ai_study_rest_hub_server.vo.AiPaperVo;
import com.ai_study_rest_hub_server.vo.PaperVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author leadingsoul
* @description 针对表【paper】的数据库操作Service
* @createDate 2026-01-01 17:13:09
*/
public interface PaperService extends IService<Paper> {

    /**
     * 根据ID获取试卷详细信息的方法
     * @param id 试卷的唯一标识符
     * @return 返回对应ID的试卷对象，如果未找到则可能返回null
     */
    Paper getDetailById(Integer id);

    /**
     * 根据PaperVo对象创建并返回一个新的Paper对象
     *
     * @param paperVo 包含创建Paper所需数据的值对象
     * @return 创建好的Paper对象
     */
    Paper createPaper(PaperVo paperVo);

    /**
     * 创建试卷的方法
     * @param aiPaperVo 试卷视图对象，包含创建试卷所需的信息
     * @return Paper 返回创建的试卷对象
     */
    Paper AICreatePaper(AiPaperVo aiPaperVo);

    /**
     * 更新试卷信息的方法
     * @param id 试卷的唯一标识符，用于确定要更新哪篇试卷
     * @param paperVo 包含试卷更新信息的视图对象，封装了需要更新的试卷字段
     * @return 返回更新后的Paper对象，可能包含更新后的完整信息
     */
    Paper updatePaper(Integer id, PaperVo paperVo);

    /**
     * 更新试卷状态的方法
     * @param id 试卷的ID，用于标识需要更新的试卷
     * @param status 试卷的新状态，用于设置试卷的当前状态
     */
    void updatePaperStatus(Integer id, String status);

    /**
     * 删除试卷的方法
     * @param id 试卷的唯一标识符，用于指定要删除的试卷
     */
    void deletePaper(Integer id);
}
