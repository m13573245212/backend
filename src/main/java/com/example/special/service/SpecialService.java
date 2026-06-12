package com.example.special.service;

import com.example.special.entity.AutoTaskInfo;
import com.example.special.entity.AutoTaskParamInfo;
import com.example.special.entity.AutoTaskStepInfo;
import com.example.special.entity.req.AutoTaskRequest;
import com.github.pagehelper.PageInfo;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface SpecialService {
    ResponseEntity<PageInfo<AutoTaskInfo>> getTasksByPage(AutoTaskRequest  request);
    ResponseEntity<PageInfo<AutoTaskStepInfo>> getStepsByPage(AutoTaskRequest request);
    ResponseEntity<PageInfo<AutoTaskParamInfo>> getParamsByPage(AutoTaskRequest request);
    ResponseEntity<Object> executeTask(String taskId) throws Exception;
    int insertTask(AutoTaskInfo taskInfo);
    int insertSteps(List<AutoTaskStepInfo> steps);
    int insertParams(List<AutoTaskParamInfo> params);
    int updateTask(AutoTaskInfo taskInfo);
    int updateStep(AutoTaskStepInfo stepInfo);
    int updateParam(AutoTaskParamInfo paramInfo);
    int deleteTask(String taskId);
    int deleteStep(String stepId);
    int deleteParam(String paramId);
}
