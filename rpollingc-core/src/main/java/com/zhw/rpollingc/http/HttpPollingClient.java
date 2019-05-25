package com.zhw.rpollingc.http;

import com.zhw.rpollingc.common.PollingClient;
import com.zhw.rpollingc.common.PollingOptions;
import com.zhw.rpollingc.common.RpcException;
import com.zhw.rpollingc.common.TypeRefrence;
import com.zhw.rpollingc.http.conn.HttpEndPoint;
import com.zhw.rpollingc.http.conn.MultiConnClient;
import com.zhw.rpollingc.http.protocol.AbstractHttpPollingProtocol;
import com.zhw.rpollingc.request.netty.NettyConfig;

import java.util.function.Consumer;

public class HttpPollingClient implements PollingClient {

    private AbstractHttpPollingProtocol protocol;
    private HttpEndPoint endPoint;

    public HttpPollingClient(NettyConfig config) {
        this(config, null);
    }

    public HttpPollingClient(NettyConfig config, AbstractHttpPollingProtocol protocol) {
        if (config == null) {
            throw new NullPointerException("config");
        }
        this.protocol = protocol;
        endPoint = new MultiConnClient(config);

    }


    @Override
    public void connect() {

    }

    @Override
    public void close() {

    }

    @Override
    public <R> R send(String service,
                      Object msg,
                      TypeRefrence<R> retType,
                      PollingOptions options) throws RpcException {




        return null;
    }

    @Override
    public <R> void sendAsync(String service,
                              Object msg,
                              TypeRefrence<R> retType,
                              Consumer<R> onResult,
                              Consumer<Throwable> onError,
                              PollingOptions options) {

    }
}
