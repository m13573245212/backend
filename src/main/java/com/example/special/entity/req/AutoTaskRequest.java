package com.example.special.entity.req;

import com.example.common.Page;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AutoTaskRequest extends Page {

    private String taskId;
}
