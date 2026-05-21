package com.example.special.mapper;

import com.example.special.entity.AutoTaskInfo;
import com.example.special.entity.AutoTaskParamInfo;
import com.example.special.entity.AutoTaskStepInfo;
import com.example.special.entity.req.AutoTaskRequest;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AutoTaskMapper {
    List<AutoTaskInfo> getAutoTask(AutoTaskRequest request);

    List<AutoTaskStepInfo> getAutoTaskStep(AutoTaskRequest request);

    List<AutoTaskParamInfo> getAutoTaskParam(AutoTaskRequest request);

    List<AutoTaskParamInfo> getAutoTaskParamAllSteps(AutoTaskRequest request);

    int insertAutoTask(AutoTaskInfo autoTaskInfo);

    int insertAutoTaskStep(List<AutoTaskStepInfo> steps);

    int insertAutoTaskParam(List<AutoTaskParamInfo> params);

    int updateAutoTask(AutoTaskInfo autoTaskInfo);

    int updateAutoTaskStep(AutoTaskStepInfo autoTaskStepInfo);

    int updateAutoTaskParam(AutoTaskParamInfo autoTaskParamInfo);

    int deleteAutoTask(AutoTaskInfo autoTaskInfo);

    int deleteAutoTaskStep(AutoTaskStepInfo autoTaskStepInfo);

    int deleteAutoTaskParam(AutoTaskParamInfo autoTaskParamInfo);
}
