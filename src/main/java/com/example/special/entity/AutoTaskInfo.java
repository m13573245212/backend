package com.example.special.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class AutoTaskInfo {
    private String id;
    private String taskCode;//任务编码，唯一标识，用于接口调用
    private String taskName;//任务名称
    private String remark;//任务描述,备注说明
    private String enable;//是否启用 0:禁用 1:启用
    private String isDynamicClass;//是否包含动态编译类 0否 1是
    private String execType;//执行方式 1手动 2定时
    private String cron;//定时表达式
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    private String createBy;//创建人
}
