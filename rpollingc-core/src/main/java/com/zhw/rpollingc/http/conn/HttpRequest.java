package com.zhw.rpollingc.http.conn;

import io.netty.handler.codec.DecoderResult;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class HttpRequest {
    private String uri;
    private BiConsumer<DecoderResult, HttpEndPoint> onResponse;
    private Consumer<Throwable> onError;

    public HttpRequest(String uri,
                       BiConsumer<DecoderResult, HttpEndPoint> onResponse,
                       Consumer<Throwable> onError) {
        this.uri = uri;
        this.onResponse = onResponse;
        this.onError = onError;
    }

    public String getUri() {
        return uri;
    }

    public BiConsumer<DecoderResult, HttpEndPoint> getOnResponse() {
        return onResponse;
    }

    public Consumer<Throwable> getOnError() {
        return onError;
    }
}

class GetRequest extends HttpRequest {
    public GetRequest(String uri,
                      BiConsumer<DecoderResult, HttpEndPoint> onResponse,
                      Consumer<Throwable> onError) {
        super(uri, onResponse, onError);
    }
}

class PostRequest extends HttpRequest {

    public PostRequest(String uri,
                       BiConsumer<DecoderResult, HttpEndPoint> onResponse,
                       Consumer<Throwable> onError) {
        super(uri, onResponse, onError);
    }
}