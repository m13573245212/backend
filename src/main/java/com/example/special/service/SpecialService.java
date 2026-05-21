package com.example.special.service;

import com.example.special.entity.AutoTaskInfo;
import com.example.special.entity.AutoTaskParamInfo;
import com.example.special.entity.AutoTaskStepInfo;

import java.util.List;

public interface SpecialService {
    Object executeTask(String taskId) throws Exception;
    int insertTask(AutoTaskInfo taskInfo);
    int insertSteps(List<AutoTaskStepInfo> steps);
    int insertParams(List<AutoTaskParamInfo> params);
}
