package com.zhw.rpollingc.request.remote;

import com.zhw.rpollingc.request.protocol.QueryResultExecutor;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class AbstractPooledScheduleExecutor extends QueryResultExecutor {

    private ScheduledExecutorService scheduledExecutorService;

    public AbstractPooledScheduleExecutor() {
        this(4);
    }

    public AbstractPooledScheduleExecutor(int poolSize) {
        this(poolSize, new DefaultThreadFactory("-schedule"));
    }

    public AbstractPooledScheduleExecutor(int poolSize, ThreadFactory threadFactory) {
        scheduledExecutorService = new ScheduledThreadPoolExecutor(poolSize, threadFactory);
    }

    public AbstractPooledScheduleExecutor(int queryIntervalMs,
                                          ScheduledExecutorService scheduledExecutorService) {
        super(queryIntervalMs);
        this.scheduledExecutorService = scheduledExecutorService;
    }


    @Override
    public <T> void scheduleGet(String url, Consumer<T> onSuccess,
                                Consumer<Throwable> onError,
                                TypeReference<T> resultClass, long timeoutMs) throws Exception {

        scheduledExecutorService.schedule(() -> {
            try {
                get(url, onSuccess, onError, resultClass);
            } catch (Exception e) {
                onError.accept(e);
            }
        }, timeoutMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public <T> void schedulePost(String url, Object body, Consumer<T> onSuccess,
                                 Consumer<Throwable> onError,
                                 TypeReference<T> resultClass, long timeoutMs) throws Exception {
        scheduledExecutorService.schedule(() -> {
            try {
                post(url, body, onSuccess, onError, resultClass);
            } catch (Exception e) {
                onError.accept(e);
            }
        }, timeoutMs, TimeUnit.MILLISECONDS);
    }
}
