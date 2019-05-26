package com.zhw.rpollingc.common;

public class ErrorResposeException extends RpcException {
    public ErrorResposeException() {
    }

    public ErrorResposeException(String message) {
        super(message);
    }

    public ErrorResposeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ErrorResposeException(Throwable cause) {
        super(cause);
    }
}
