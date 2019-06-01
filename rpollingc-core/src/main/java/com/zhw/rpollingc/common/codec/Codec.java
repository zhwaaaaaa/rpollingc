package com.zhw.rpollingc.common.codec;


public interface Codec<C, S, R, T> {

    S encode(C config) throws EncodeException;

    T decode(R response, C conf) throws DecodeException;
}
