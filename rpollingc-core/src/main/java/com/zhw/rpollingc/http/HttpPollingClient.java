package com.zhw.rpollingc.http;

import com.zhw.rpollingc.common.PollingClient;
import com.zhw.rpollingc.common.PollingOptions;
import com.zhw.rpollingc.common.RpcException;
import com.zhw.rpollingc.common.TypeReference;
import com.zhw.rpollingc.http.protocol.HttpPollingProtocol;

import java.util.function.Consumer;

public class HttpPollingClient implements PollingClient {

    private final HttpPollingProtocol pollingProtocol;

    public HttpPollingClient(HttpPollingProtocol pollingProtocol) {
        this.pollingProtocol = pollingProtocol;
    }

    @Override
    public <R> R send(String service,
                      Object msg,
                      TypeReference<R> retType,
                      PollingOptions options) throws RpcException {
        return null;
    }

    @Override
    public <R> void sendAsync(String service,
                              Object msg,
                              TypeReference<R> retType,
                              Consumer<R> onResult,
                              Consumer<Throwable> onError,
                              PollingOptions options) {

    }

    @Override
    public void connect() {

    }

    @Override
    public void close() {

    }
}
