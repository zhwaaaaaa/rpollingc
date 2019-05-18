package com.zhw.rpollingc.request.netty;


import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public abstract class RequestEvent {

    private static final AtomicLong INVODE_ID = new AtomicLong(0);

    private String url;
    private Consumer<Throwable> onError;
    private Consumer<String> onResponse;
    private long sentTime;

    private long uuid = INVODE_ID.getAndIncrement();

    public RequestEvent() {
    }

    public RequestEvent(String url,
                        Consumer<Throwable> onError,
                        Consumer<String> onResponse) {
        this.url = url;
        this.onError = onError;
        this.onResponse = onResponse;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Consumer<Throwable> getOnError() {
        return onError;
    }

    public void setOnError(Consumer<Throwable> onError) {
        this.onError = onError;
    }

    public Consumer<String> getOnResponse() {
        return onResponse;
    }

    public void setOnResponse(Consumer<String> onResponse) {
        this.onResponse = onResponse;
    }

    public long getUuid() {
        return uuid;
    }

    public long getSentTime() {
        return sentTime;
    }

    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
    }
}

class GetEvent extends RequestEvent {

    public GetEvent() {
    }

    public GetEvent(String url,
                    Consumer<Throwable> onError,
                    Consumer<String> onResponse) {
        super(url, onError, onResponse);
    }

}

class PostEvent extends RequestEvent {
    private byte[] body;

    public PostEvent() {
    }

    public PostEvent(String url,
                     Consumer<Throwable> onError,
                     Consumer<String> onResponse,
                     byte[] body) {
        super(url, onError, onResponse);
        this.body = body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] getBody() {
        return body;
    }
}

