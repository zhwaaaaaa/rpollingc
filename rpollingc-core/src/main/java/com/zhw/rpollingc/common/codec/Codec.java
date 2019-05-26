package com.zhw.rpollingc.common.codec;


import com.zhw.rpollingc.common.TypeReference;

public interface Codec<S, R, T> {

    S encode(String service, Object msg) throws EncodeException;

    T decode(R response, TypeReference<T> typeReference) throws DecodeException;
}
