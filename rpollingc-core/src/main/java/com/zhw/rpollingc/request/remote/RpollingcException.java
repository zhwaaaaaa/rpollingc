package com.zhw.rpollingc.request.remote;

import com.zhw.rpollingc.request.netty.RequestEvent;

public class RpollingcException extends RuntimeException {

    private RequestEvent requestEvent;

    public void setRequestEvent(RequestEvent requestEvent) {
        this.requestEvent = requestEvent;
    }

    public RequestEvent getRequestEvent() {
        return requestEvent;
    }

    public RpollingcException() {
    }

    public RpollingcException(String message) {
        super(message);
    }

    public RpollingcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpollingcException(Throwable cause) {
        super(cause);
    }

    public RpollingcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RpollingcException(String message, RequestEvent requestEvent) {
        super(message);
        this.requestEvent = requestEvent;
    }
}
