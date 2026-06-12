package com.example.special.controller;

import com.example.special.entity.AutoTaskInfo;
import com.example.special.entity.AutoTaskParamInfo;
import com.example.special.entity.AutoTaskStepInfo;
import com.example.special.entity.req.AutoTaskRequest;
import com.example.special.service.SpecialService;
import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/list1")
    public ResponseEntity<PageInfo<AutoTaskInfo>> list1(@RequestBody AutoTaskRequest request) {
        return specialService.getTasksByPage(request);
    }

    @PostMapping("/list2")
    public ResponseEntity<PageInfo<AutoTaskStepInfo>> list2(@RequestBody AutoTaskRequest request) {
        return specialService.getStepsByPage(request);
    }

    @PostMapping("/list3")
    public ResponseEntity<PageInfo<AutoTaskParamInfo>> list3(@RequestBody AutoTaskRequest request) {
        return specialService.getParamsByPage(request);
    }

    @PostMapping("/start")
    public ResponseEntity<Object> start(String taskId) throws Exception {
        return specialService.executeTask(taskId);
    }

    @PostMapping("/insertTask")
    public Object insertTask(@RequestBody AutoTaskInfo task) {
        return specialService.insertTask(task);
    }

    @PostMapping("/insertStep")
    public Object insertStep(@RequestBody List<AutoTaskStepInfo> steps) {
        return specialService.insertSteps(steps);
    }

    @PostMapping("/insertParam")
    public Object insertParam(@RequestBody List<AutoTaskParamInfo> params) {
        return specialService.insertParams(params);
    }
    @PostMapping("/updateTask")
    public Object updateTask(@RequestBody AutoTaskInfo task) {
        return specialService.updateTask(task);
    }
    @PostMapping("/updateStep")
    public Object updateStep(@RequestBody AutoTaskStepInfo step) {
        return specialService.updateStep(step);
    }
    @PostMapping("/updateParam")
    public Object updateParam(@RequestBody AutoTaskParamInfo param) {
        return specialService.updateParam(param);
    }

    @GetMapping("/deleteTask/{taskId}")
    public Object deleteTask(@PathVariable String taskId) {return specialService.deleteTask(taskId);}

    @GetMapping("/deleteStep/{stepId}")
    public Object deleteStep(@PathVariable String stepId) {return specialService.deleteStep(stepId);}

    @GetMapping("/deleteParam/{paramId}")
    public Object deleteParam(@PathVariable String paramId) {return specialService.deleteParam(paramId);}

}
