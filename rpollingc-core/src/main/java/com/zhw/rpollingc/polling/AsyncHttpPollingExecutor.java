package com.zhw.rpollingc.polling;

import com.zhw.rpollingc.http.HttpClient;
import com.zhw.rpollingc.http.HttpResponse;
import com.zhw.rpollingc.http.protocol.ReqOptions;
import io.netty.util.Timer;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class AsyncHttpPollingExecutor implements HttpPollingExecutor {

    private HttpClient<ReqOptions> client;
    private Timer timer;


    @Override
    public void get(String url, Consumer<HttpResponse> respCsm, Consumer<Throwable> errCsm) {
    }

    @Override
    public void post(String url, Object body, Consumer<HttpResponse> respCsm, Consumer<Throwable> errCsm) {

    }

    @Override
    public void scheduleExec(long timeoutMs, Runnable run) {
        timer.newTimeout(timeout -> run.run(), timeoutMs, TimeUnit.MILLISECONDS);
    }
}
