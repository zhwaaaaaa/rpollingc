package com.zhw.rpollingc.request.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TaskStatus {

    public static final String _SUCCESS = "FINISH";
    public static final String _PROGRESS = "PROGRESS";
    public static final String _PENDING = "PENDING";

    public static class TaskResult {

        private Status status;
        @JsonProperty("result")
        private String result;

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

    }

    public static class Status {
        @JsonProperty("ret_code")
        private Integer retCode;
        @JsonProperty("err_code")
        private List<Integer> errCode;
        private String msg;

        public Integer getRetCode() {
            return retCode;
        }

        public void setRetCode(Integer retCode) {
            this.retCode = retCode;
        }

        public List<Integer> getErrCode() {
            return errCode;
        }

        public void setErrCode(List<Integer> errCode) {
            this.errCode = errCode;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        @Override
        public String toString() {
            return "Status{" +
                    "retCode=" + retCode +
                    ", errCode=" + errCode +
                    ", msg='" + msg + '\'' +
                    '}';
        }
    }

    @JsonProperty("task_id")
    private String taskId;
    private String state;

    @JsonProperty("task_result")
    private TaskResult taskResult;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public TaskResult getTaskResult() {
        return taskResult;
    }

    public void setTaskResult(TaskResult taskResult) {
        this.taskResult = taskResult;
    }

    @Override
    public String toString() {
        return "TaskStatus{" +
                "taskId='" + taskId + '\'' +
                ", state='" + state + '\'' +
                ", taskResult=" + taskResult +
                '}';
    }
}
