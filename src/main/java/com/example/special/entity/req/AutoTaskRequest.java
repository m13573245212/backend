package com.example.special.entity.req;

import com.example.common.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AutoTaskRequest extends Page {
    private String taskId;
    private String stepId;
}
