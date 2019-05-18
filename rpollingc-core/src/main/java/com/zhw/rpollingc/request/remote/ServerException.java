package com.zhw.rpollingc.request.remote;

import com.zhw.rpollingc.request.netty.RequestEvent;

public class ServerException extends RpollingcException {
    public ServerException() {
    }

    public ServerException(String message, RequestEvent requestEvent) {
        super(message, requestEvent);
    }

    public ServerException(String message) {
        super(message);
    }

    public ServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerException(Throwable cause) {
        super(cause);
    }

    public ServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
