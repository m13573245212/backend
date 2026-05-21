package com.example.special.controller;

import com.example.special.entity.AutoTaskInfo;
import com.example.special.entity.AutoTaskParamInfo;
import com.example.special.entity.AutoTaskStepInfo;
import com.example.special.service.SpecialService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 动态配置任务
 * 自动组合现有函数
 * 支持外部.java文件加载
 */

@RestController
@RequestMapping("/special")
@AllArgsConstructor
public class SpecialController {
    private final SpecialService specialService;

    @PostMapping("/start")
    public Object start(String taskId) throws Exception {
        return specialService.executeTask(taskId);
    }

    @PostMapping("/insertTask")
    public Object insertTask(AutoTaskInfo task) {
        return specialService.insertTask(task);
    }

    @PostMapping("/insertStep")
    public Object insertStep(List<AutoTaskStepInfo> steps) {
        return specialService.insertSteps(steps);
    }
    @PostMapping("/insertParam")
    public Object insertParam(List<AutoTaskParamInfo> params) {
        return specialService.insertParams(params);
    }
}
