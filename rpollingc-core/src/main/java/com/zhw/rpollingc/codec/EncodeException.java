package com.zhw.rpollingc.codec;

import com.zhw.rpollingc.rpc.RpcException;

public class EncodeException extends RpcException {

    public static final int CODE_EXP_ENCODE = 100;

    public EncodeException(String message) {
        super(message, CODE_EXP_ENCODE);
    }

    public EncodeException(String message, Throwable cause) {
        super(message, cause, CODE_EXP_ENCODE);
    }

    public EncodeException(Throwable cause) {
        super(cause, CODE_EXP_ENCODE);
    }
}
