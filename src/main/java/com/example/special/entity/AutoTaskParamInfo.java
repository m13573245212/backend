package com.example.special.entity;

import lombok.Data;

import java.util.Date;
@Data
public class AutoTaskParamInfo {
    private String id;
    private String stepId;//关联auto_task_step.id
    private String paramIndex;//参数索引,对应方法入参顺序
    private String paramType;//参数类型 fixed固定值 / json / sql
    private String paramValue;//参数内容：固定值、JSON字符串、SQL语句
    private String paramClassName;//JSON参数对应的实体类全类名
    private Date createTime;
    private Date updateTime;
}
