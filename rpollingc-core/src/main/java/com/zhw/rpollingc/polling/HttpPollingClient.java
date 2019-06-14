package com.zhw.rpollingc.polling;

import com.zhw.rpollingc.common.ReflectTypeReference;
import com.zhw.rpollingc.common.RpcException;
import com.zhw.rpollingc.http.HttpClient;

import java.util.function.Consumer;

public class HttpPollingClient<O> implements PollingClient {

    private HttpClient<O> client;

    public HttpPollingClient(HttpClient<O> client) {
        this.client = client;
    }

    @Override
    public <T> T send(String url,
                      Object body,
                      ReflectTypeReference<T> typeReference,
                      int maxWaitingMs) throws RpcException {
        return null;
    }

    @Override
    public <T> void sendAsync(String url,
                              Object body,
                              ReflectTypeReference<T> typeReference,
                              Consumer<T> onResult,
                              Consumer<RpcException> onError,
                              int maxWaitingMs) {

    }
}
