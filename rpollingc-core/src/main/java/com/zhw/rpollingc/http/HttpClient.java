package com.zhw.rpollingc.http;

import com.zhw.rpollingc.common.RpcException;

import java.util.function.Consumer;

public interface HttpClient<O> {

    HttpResponse get(String url, O options) throws RpcException;

    HttpResponse post(String url, Object body, O options) throws RpcException;

    void getAsync(String url, O options, Consumer<HttpResponse> onResp, Consumer<Throwable> onErr);

    void postAsync(String url, Object body, O options, Consumer<HttpResponse> onResp, Consumer<Throwable> onErr);

}
