package com.zhw.rpollingc.codec;

import com.zhw.rpollingc.rpc.Request;

public interface Codec<S, R> {
    S encode(Request request) throws EncodeException;

    Object decode(Request request, R response) throws DecodeException;
}
