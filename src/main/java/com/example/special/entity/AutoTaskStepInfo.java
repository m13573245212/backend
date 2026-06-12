package com.example.special.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class AutoTaskStepInfo {
    private String id;
    private String taskId;//关联主表auto_task.id
    private String stepSort;//执行顺序，从小到大
    private String classType;//类的类型 1=普通类 2=SpringBean 3=动态编译类 4=静态类型
    private String classFullName;//类的全名
    private String methodName;//方法名
    private String dynamicSource;//动态类Java源码（class_type=3时使用）
    private String remark;//备注
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
