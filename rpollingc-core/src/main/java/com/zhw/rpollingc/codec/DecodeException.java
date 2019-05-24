package com.zhw.rpollingc.codec;

import com.zhw.rpollingc.rpc.RpcException;

public class DecodeException extends RpcException {

    public static final int CODE_EXP_ENCODE = 100;

    public DecodeException(String message) {
        super(message, CODE_EXP_ENCODE);
    }

    public DecodeException(String message, Throwable cause) {
        super(message, cause, CODE_EXP_ENCODE);
    }

    public DecodeException(Throwable cause) {
        super(cause, CODE_EXP_ENCODE);
    }
}
