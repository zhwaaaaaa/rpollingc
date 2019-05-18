package com.zhw.rpollingc.request;

import com.zhw.rpollingc.request.protocol.ResultDecoder;

import java.util.concurrent.ExecutorService;

public class RequestBuilder {

    private RequestExecutor executor;

    private ExecutorService resultThreadPool;

    private ResultDecoder resultDecoder;

    public void setExecutor(RequestExecutor executor) {
        this.executor = executor;
    }

    public void setResultThreadPool(ExecutorService resultThreadPool) {
        this.resultThreadPool = resultThreadPool;
    }

    public void setResultDecoder(ResultDecoder resultDecoder) {
        this.resultDecoder = resultDecoder;
    }

    public ResultDecoder getResultDecoder() {
        return resultDecoder;
    }

    public Request build() {
        return new DefaultRequest(resultThreadPool, executor, resultDecoder);
    }

}
