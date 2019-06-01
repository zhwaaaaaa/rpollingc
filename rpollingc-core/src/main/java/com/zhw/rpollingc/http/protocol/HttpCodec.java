package com.zhw.rpollingc.http.protocol;

import com.zhw.rpollingc.common.codec.Codec;
import com.zhw.rpollingc.common.codec.DecodeException;
import com.zhw.rpollingc.common.codec.EncodeException;
import com.zhw.rpollingc.http.HttpResponse;
import com.zhw.rpollingc.http.conn.HttpRequest;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;

public abstract class HttpCodec implements Codec<HttpRequest, ByteBuf, FullHttpResponse, HttpResponse> {


    @Override
    public ByteBuf encode(HttpRequest request) throws EncodeException {


        return null;
    }

    @Override
    public HttpResponse decode(FullHttpResponse response, HttpRequest conf) throws DecodeException {
        return null;
    }

}
