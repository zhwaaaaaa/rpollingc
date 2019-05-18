package com.zhw.rpollingc.request.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostId {
    @JsonProperty("task_id")
    private String taskId;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
