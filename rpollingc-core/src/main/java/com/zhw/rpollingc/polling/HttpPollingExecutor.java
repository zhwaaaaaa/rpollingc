package com.zhw.rpollingc.polling;

import com.zhw.rpollingc.http.HttpResponse;

import java.util.function.Consumer;

public interface HttpPollingExecutor extends PollingExecutor {

    void get(String url, Consumer<HttpResponse> respCsm, Consumer<Throwable> errCsm);

    void post(String url, Object body, Consumer<HttpResponse> respCsm, Consumer<Throwable> errCsm);

}
