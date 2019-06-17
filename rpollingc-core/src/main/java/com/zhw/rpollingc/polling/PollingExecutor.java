package com.zhw.rpollingc.polling;

public interface PollingExecutor {

    void scheduleExec(long timeoutMs, Runnable run);

}
