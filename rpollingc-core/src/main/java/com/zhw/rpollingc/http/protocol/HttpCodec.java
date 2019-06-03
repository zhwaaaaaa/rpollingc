package com.zhw.rpollingc.http.protocol;

import com.zhw.rpollingc.common.codec.Codec;
import com.zhw.rpollingc.common.codec.DecodeException;
import com.zhw.rpollingc.common.codec.EncodeException;
import com.zhw.rpollingc.http.HttpResponse;
import com.zhw.rpollingc.http.conn.HttpRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;

import static io.netty.handler.codec.http.HttpConstants.SP;

public abstract class HttpCodec implements Codec<HttpRequest, ByteBuf, FullHttpResponse, HttpResponse> {
    private static final int initialCapacity = 256;
    private static final char SLASH = '/';
    private static final char QUESTION_MARK = '?';
    private static final int SLASH_AND_SPACE_SHORT = (SLASH << 8) | SP;
    private static final int SPACE_SLASH_AND_SPACE_MEDIUM = (SP << 16) | SLASH_AND_SPACE_SHORT;

    private final ByteBufAllocator allocator;

    public HttpCodec(ByteBufAllocator allocator) {
        this.allocator = allocator;
    }

    @Override
    public ByteBuf encode(HttpRequest request) throws EncodeException {
        ByteBuf buffer = allocator.buffer(initialCapacity);

        return null;
    }

    protected int encodeInitialLine(HttpRequest request, ByteBuf buf) {
        ByteBufUtil.copy(request.getMethod().asciiName(), buf);
        String service = request.getService();
        if (StringUtil.isNullOrEmpty(service)) {
            ByteBufUtil.writeMediumBE(buf, SPACE_SLASH_AND_SPACE_MEDIUM);
        } else {
            buf.writeByte(SP).writeCharSequence(service, CharsetUtil.UTF_8);
            buf.writeByte(SP);
        }

        request.getVersion();

    }


    @Override
    public HttpResponse decode(FullHttpResponse response, HttpRequest conf) throws DecodeException {
        return null;
    }

}
