package com.zhw.rpollingc.common.codec;


import com.zhw.rpollingc.common.ProtocolException;

public class DecodeException extends ProtocolException {
    public DecodeException() {
    }

    public DecodeException(String message) {
        super(message);
    }

    public DecodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecodeException(Throwable cause) {
        super(cause);
    }
}
