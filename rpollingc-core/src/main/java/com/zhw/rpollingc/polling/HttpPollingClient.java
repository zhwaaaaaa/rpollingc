package com.zhw.rpollingc.polling;

import com.zhw.rpollingc.common.ReflectTypeReference;
import com.zhw.rpollingc.common.RpcException;
import com.zhw.rpollingc.http.HttpClient;
import com.zhw.rpollingc.http.protocol.ReqOptions;
import com.zhw.rpollingc.promise.Promise;

import java.util.function.Consumer;

public class HttpPollingClient implements PollingClient {

    private HttpClient<ReqOptions> client;

    public HttpPollingClient(HttpClient<ReqOptions> client) {
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

    @Override
    public void connect() {
        client.connect();
    }

    @Override
    public void close() {
        client.close();
    }
}
