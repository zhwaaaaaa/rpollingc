package com.zhw.rpollingc.http.conn;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

public abstract class HttpRequest {
    private FullHttpRequest fullHttpRequest;

    public HttpRequest() {
    }

    public void setFullHttpRequest(FullHttpRequest fullHttpRequest) {
        this.fullHttpRequest = fullHttpRequest;
    }

    public FullHttpRequest getFullHttpRequest() {
        return fullHttpRequest;
    }

    public abstract void onResp(FullHttpResponse resp, HttpEndPoint endPoint);

    public abstract void onError(Throwable err, HttpEndPoint endPoint);

}
