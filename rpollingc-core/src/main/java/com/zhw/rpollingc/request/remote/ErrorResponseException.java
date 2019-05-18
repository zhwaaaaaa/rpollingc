package com.zhw.rpollingc.request.remote;

import com.zhw.rpollingc.request.netty.RequestEvent;

public class ErrorResponseException extends ServerException {
    private int code = -1;

    public ErrorResponseException() {
    }

    public ErrorResponseException(String message, RequestEvent requestEvent) {
        super(message, requestEvent);
    }

    public ErrorResponseException(int code) {
        this.code = code;
    }

    public ErrorResponseException(String message) {
        super(message);
    }

    public ErrorResponseException(String message, int code) {
        super(message);
        this.code = code;
    }

    public ErrorResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ErrorResponseException(Throwable cause) {
        super(cause);
    }

    public ErrorResponseException(String message,
                                        Throwable cause,
                                        boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public int getCode() {
        return code;
    }
}
