package com.zhw.rpollingc.request.protocol;

import com.zhw.rpollingc.request.RequestExecutor;
import com.zhw.rpollingc.request.remote.ErrorResponseException;
import com.zhw.rpollingc.request.remote.Exception;
import com.zhw.rpollingc.request.remote.InputException;
import com.zhw.rpollingc.request.remote.TimeoutException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.lang.reflect.Type;
import java.util.function.Consumer;

public abstract class QueryResultExecutor implements RequestExecutor {

    private static InternalLogger log = InternalLoggerFactory.getInstance(QueryResultExecutor.class);

    public static final long DEFAULT_WAITING_TIMEOUT = 3600000L;

    private int queryIntervalMs = 2000;
    private final TypeReference<PostId> postIdTypeReference = new TypeReference<PostId>() {
        @Override
        public Type getType() {
            return PostId.class;
        }
    };
    private final TypeReference<TaskStatus> statusTypeReference = new TypeReference<TaskStatus>() {
        @Override
        public Type getType() {
            return TaskStatus.class;
        }
    };



    public void setQueryIntervalMs(int queryIntervalMs) {
        this.queryIntervalMs = queryIntervalMs;
    }


    public QueryResultExecutor() {
    }

    public QueryResultExecutor(int queryIntervalMs) {
        this.queryIntervalMs = queryIntervalMs;
    }

    @Override
    public void submitRequest(String url, Object body,
                              Consumer<String> onSuccess,
                              Consumer<Throwable> onError) throws Exception {


        submitRequest(url, body, onSuccess, onError, DEFAULT_WAITING_TIMEOUT);
    }

    @Override
    public void submitRequest(String url, Object body,
                              Consumer<String> onSuccess,
                              Consumer<Throwable> onError,
                              long waitingTimeout) throws Exception {
        if (waitingTimeout < queryIntervalMs) {
            throw new IllegalArgumentException("waitingTimeout " + waitingTimeout + " too short");
        }
        long queryUtilMs = System.currentTimeMillis() + waitingTimeout;
        post(url, body, postId -> scheduleNextQuery(postId.getTaskId(), onError, onSuccess, queryUtilMs),
                onError, postIdTypeReference);
    }

    public final void checkAndQuery(String taskId, TaskStatus t, Consumer<Throwable> onError,
                                    Consumer<String> resultConsumer, long waitingUtilMs) {

        if (TaskStatus._PROGRESS.equals(t.getState()) || TaskStatus._PENDING.equals(t.getState())) { // 未完成，下一次产寻
            scheduleNextQuery(taskId, onError, resultConsumer, waitingUtilMs);
        } else {
            // 收到结果
            Integer retCode = t.getTaskResult().getStatus().getRetCode();
            if (retCode < 1) {
                // 结果正确
                try {
                    resultConsumer.accept(t.getTaskResult().getResult());
                } catch (Throwable e) {
                    log.error("error on notify", e);
                }
            } else if (retCode == 1) {
                //  返回1,一般参数错误。
                onError.accept(new InputException("server return code = " + t.getTaskResult().getStatus()));
            } else {
                onError.accept(new ErrorResponseException("server return code = " + t.getTaskResult().getStatus()));
            }
        }

    }

    private void scheduleNextQuery(String taskId, Consumer<Throwable> onError,
                                   Consumer<String> resultConsumer,
                                   long waitingUtilMs) {
        if (System.currentTimeMillis() > waitingUtilMs) {
            onError.accept(new TimeoutException("timeout"));
            return;
        }

        try {
            String queryUrlPathPref = "/status/";
            scheduleGet(queryUrlPathPref + taskId,
                    ts -> this.checkAndQuery(taskId, ts, onError, resultConsumer, waitingUtilMs),
                    onError,
                    statusTypeReference,
                    queryIntervalMs);
        } catch (Exception e) {
            onError.accept(e);
        }


    }


    public abstract <T> void scheduleGet(String url,
                                         Consumer<T> onSuccess,
                                         Consumer<Throwable> onError,
                                         TypeReference<T> resultClass, long timeoutMs) throws Exception;

    public abstract <T> void get(String url,
                                 Consumer<T> onSuccess,
                                 Consumer<Throwable> onError,
                                 TypeReference<T> resultClass) throws Exception;

    public abstract <T> void post(String url,
                                  Object body,
                                  Consumer<T> onSuccess,
                                  Consumer<Throwable> onError,
                                  TypeReference<T> resultClass) throws Exception;

    public abstract <T> void schedulePost(String url,
                                          Object body,
                                          Consumer<T> onSuccess,
                                          Consumer<Throwable> onError,
                                          TypeReference<T> resultClass, long timeoutMs) throws Exception;
}
