package com.example.special.entity;

import lombok.Data;

import java.util.Date;
@Data
public class AutoTaskParamInfo {
    private String id;
    private String stepId;//关联auto_task_step.id
    private String paramIndex;//参数索引,对应方法入参顺序
    /**
     * 参数类型：
     * fixed: 固定值
     * json: JSON字符串
     * sql: SQL语句
     * result: 来自其他步骤的输出结果
     * array: 数组
     */
    private String paramType;
    /**
     * 参数值：
     * fixed时,输入固定值
     * json时,输入JSON字符串
     * sql时,输入SQL语句
     * result时,空
     * array时,数组字符串
     */
    private String paramValue;
    private String fromStepId;//result时指明来自哪个step的输出结果
    /**
     * 参数的Java类型全类名（用于fixed类型转换、json反序列化、sql结果映射、result类型转换, 方法重载匹配时也需要倚靠这个参数）
     * 如：java.lang.String, java.lang.Integer, java.util.Map, com.example.UserDTO等
     * 也可以支持基本数据类型：int, long, float, double, boolean, char, byte, short
     * 在paramType是数组时，paramClassName为数组元素类型
     */
    private String paramClassName;
    private Date createTime;
    private Date updateTime;
}
