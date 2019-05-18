package com.zhw.rpollingc.request.remote;

import com.zhw.rpollingc.request.netty.RequestEvent;

public class Exception extends RuntimeException {

    private RequestEvent requestEvent;

    public void setRequestEvent(RequestEvent requestEvent) {
        this.requestEvent = requestEvent;
    }

    public RequestEvent getRequestEvent() {
        return requestEvent;
    }

    public Exception() {
    }

    public Exception(String message) {
        super(message);
    }

    public Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public Exception(Throwable cause) {
        super(cause);
    }

    public Exception(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public Exception(String message, RequestEvent requestEvent) {
        super(message);
        this.requestEvent = requestEvent;
    }
}
