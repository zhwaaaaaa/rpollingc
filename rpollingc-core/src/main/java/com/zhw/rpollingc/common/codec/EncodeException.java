package com.zhw.rpollingc.common.codec;


import com.zhw.rpollingc.common.ProtocolException;

public class EncodeException extends ProtocolException {
    public EncodeException() {
    }

    public EncodeException(String message) {
        super(message);
    }

    public EncodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncodeException(Throwable cause) {
        super(cause);
    }
}
