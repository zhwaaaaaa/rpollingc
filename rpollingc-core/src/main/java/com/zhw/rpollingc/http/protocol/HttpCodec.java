package com.zhw.rpollingc.http.protocol;

import com.zhw.rpollingc.common.codec.Codec;
import com.zhw.rpollingc.common.codec.DecodeException;
import com.zhw.rpollingc.common.codec.EncodeException;
import com.zhw.rpollingc.http.HttpResponse;
import com.zhw.rpollingc.http.conn.HttpRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpConstants;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;

import java.util.List;


public abstract class HttpCodec implements Codec<HttpRequest<ReqOptions>, ByteBuf, FullHttpResponse, HttpResponse> {
    private static final char SLASH = '/';
    private static final char QUESTION_MARK = '?';
    private static final int SLASH_AND_SPACE_SHORT = (SLASH << 8) | HttpConstants.SP;
    private static final int SPACE_SLASH_AND_SPACE_MEDIUM = (HttpConstants.SP << 16) | SLASH_AND_SPACE_SHORT;
    private static final float HEADERS_WEIGHT_NEW = 1 / 5f;
    private static final float HEADERS_WEIGHT_HISTORICAL = 1 - HEADERS_WEIGHT_NEW;
    private static final float TRAILERS_WEIGHT_NEW = HEADERS_WEIGHT_NEW;
    private static final float TRAILERS_WEIGHT_HISTORICAL = HEADERS_WEIGHT_HISTORICAL;

    private static byte[] PROTOCOL_HTTP_11_CRLF = new byte[]{'H', 'T', 'T', 'P', '/', '1', '.', '1', HttpConstants.CR, HttpConstants.LF};
    private final ByteBufAllocator allocator;

    private volatile float initialHeadersCapacity = 256;

    public HttpCodec(ByteBufAllocator allocator) {
        this.allocator = allocator;
    }

    private static int padSizeForAccumulation(int readableBytes) {
        return (readableBytes << 2) / 3;
    }


    @Override
    public ByteBuf encode(HttpRequest request) throws EncodeException {
        CompositeByteBuf buffer = allocator.compositeBuffer(2);
        ByteBuf headers = allocator.buffer((int) initialHeadersCapacity);
        encodeInitialLine(request, headers);
        HeadersWriter headersWriter = new HeadersWriter(headers);

        BodyAllocator bodyAllocator = new BodyAllocator(this.allocator);
        BodyOutputStream stream;
        try {
            stream = encodeHeaderAndBody(request,
                    headersWriter, bodyAllocator);
            headers.writeShort(HeadersWriter.CRLF_SHORT);
        } catch (EncodeException e) {
            List<ByteBuf> bufs = bodyAllocator.getBufs();
            for (ByteBuf buf : bufs) {
                releaseBuf(buf);
            }
            releaseBuf(headers);
            throw e;
        }

        ByteBufUtil.writeShortBE(headers, HeadersWriter.CRLF_SHORT);

        initialHeadersCapacity = TRAILERS_WEIGHT_NEW * padSizeForAccumulation(headers.readableBytes()) +
                TRAILERS_WEIGHT_HISTORICAL * initialHeadersCapacity;

        buffer.addComponent(true, 0, headers);

        if (stream != null) {
            ByteBuf body = stream.getBuf();
            buffer.addComponent(true, 1, body);
        }
        return buffer;
    }

    private void releaseBuf(ByteBuf buf) {
        int i = buf.refCnt();
        if (i > 0) {
            buf.release(i);
        }
    }

    protected abstract BodyOutputStream encodeHeaderAndBody(HttpRequest<ReqOptions> request,
                                                            HeadersWriter headersWriter,
                                                            BodyAllocator allocator) throws EncodeException;

    private void encodeInitialLine(HttpRequest<ReqOptions> request, ByteBuf buf) {
        ByteBufUtil.copy(request.getMethod().asciiName(), buf);
        String service = request.getService();
        if (StringUtil.isNullOrEmpty(service)) {
            ByteBufUtil.writeMediumBE(buf, SPACE_SLASH_AND_SPACE_MEDIUM);
        } else {
            buf.writeByte(HttpConstants.SP).writeCharSequence(service, CharsetUtil.UTF_8);
            buf.writeByte(HttpConstants.SP);
        }
        buf.writeBytes(PROTOCOL_HTTP_11_CRLF);
    }


    @Override
    public HttpResponse decode(FullHttpResponse response, HttpRequest<ReqOptions> conf) throws DecodeException {
        DecoderResult result = response.decoderResult();
        if (result.isSuccess()) {
            return decodeInternal(response, conf);
        } else {
            throw new DecodeException("bad response", result.cause());
        }
    }

    protected abstract HttpResponse decodeInternal(FullHttpResponse response, HttpRequest<ReqOptions> conf) throws DecodeException;

}
