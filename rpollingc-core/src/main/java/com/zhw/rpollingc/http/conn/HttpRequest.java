package com.zhw.rpollingc.http.conn;

import com.zhw.rpollingc.common.Request;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;

public abstract class HttpRequest implements Request<FullHttpResponse> {

    private FullHttpRequest request;
    private Object body;
    private int timeoutMs;

    public HttpRequest(HttpMethod method, String service, Object body) {
        this.request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method,
                service, false);
        this.body = body;
    }

    public HttpMethod getMethod() {
        return request.method();
    }


    @Override
    public String getService() {
        return request.uri();
    }

    @Override
    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public HttpHeaders headers() {
        return request.headers();
    }

    @Override
    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    protected abstract ByteBuf getReqByteBuf();

    @Override
    public abstract void onErr(Throwable err);

    @Override
    public abstract void onResp(FullHttpResponse response);
}
