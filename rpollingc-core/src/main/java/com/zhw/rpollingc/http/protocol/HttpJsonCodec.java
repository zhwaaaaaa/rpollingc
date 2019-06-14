package com.zhw.rpollingc.http.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhw.rpollingc.common.codec.DecodeException;
import com.zhw.rpollingc.common.codec.EncodeException;
import com.zhw.rpollingc.http.HttpResponse;
import com.zhw.rpollingc.http.conn.HttpRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.AsciiString;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class HttpJsonCodec extends HttpCodec {
    public static final AsciiString APPLICATION_JSON_UTF8 = AsciiString.cached("application/json;charset=utf-8");
    private final ObjectMapper objectMapper;
    private final boolean useGzip;
    private final String host;

    public HttpJsonCodec(ByteBufAllocator allocator, ObjectMapper objectMapper, boolean useGzip, String host) {
        super(allocator);
        this.objectMapper = objectMapper;
        this.useGzip = useGzip;
        this.host = host;
    }

    @Override
    protected BodyOutputStream encodeHeaderAndBody(HttpRequest<ReqOptions> request,
                                                   HeadersWriter headersWriter,
                                                   BodyAllocator allocator) throws EncodeException {

        headersWriter.writeHeader(HttpHeaderNames.HOST, host);
        headersWriter.writeHeader(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

        BodyOutputStream bos = null;
        Object body = request.getBody();
        if (body != null) {
            bos = allocator.getBodyOutputStream();
            OutputStream os = bos;
            try {
                if (useGzip) {
                    os = new GZIPOutputStream(bos);
                }
                writeJson(body, os);
            } catch (IOException e) {
                throw new EncodeException(e);
            }

            headersWriter.writeHeader(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(bos.getSize()));
            headersWriter.writeHeader(HttpHeaderNames.CONTENT_TYPE, APPLICATION_JSON_UTF8);
            if (useGzip) {
                headersWriter.writeHeader(HttpHeaderNames.CONTENT_ENCODING, HttpHeaderValues.GZIP);
                headersWriter.writeHeader(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP_DEFLATE);
            }
        }

        return bos;
    }

    private void writeJson(Object body, OutputStream os) throws IOException {
        if (body instanceof String) {
            os.write(((String) body).getBytes(Charset.forName("utf-8")));
        } else if (body instanceof byte[]) {
            os.write((byte[]) body);
        } else {
            objectMapper.writeValue(os, body);
        }
        os.flush();
        os.close();
    }

    @Override
    protected HttpResponse decodeInternal(FullHttpResponse response, HttpRequest<ReqOptions> conf) throws DecodeException {
        HttpHeaders headers = response.headers();

        ByteBuf content = response.content();
        int code = response.status().code();

        LinkedHashMap<String, String> h = new LinkedHashMap<>();
        for (Map.Entry<String, String> header : headers) {
            h.put(header.getKey(), header.getValue());
        }
        if (content.readableBytes() > 0) {
            InputStream in = new ByteBufInputStream(content);
            String s = headers.get("Content-Encoding");
            try {
                if ("gzip".equalsIgnoreCase(s)) {
                    in = new GZIPInputStream(in);
                }
                if (isSuccess(code)) {
                    ReqOptions options = conf.getOptions();
                    Object o = objectMapper.readValue(in,
                            objectMapper.getTypeFactory().constructType(options.getType().getType()));
                    return new DefaultHttpResponse<>(code, h, o);
                } else {
                    DefaultHttpResponse<Object> httpResponse = new DefaultHttpResponse<>(code, h, null);
                    httpResponse.setErr(readAll(in));
                    return httpResponse;
                }
            } catch (Exception e) {
                throw new DecodeException(e);
            }
        }

        return new DefaultHttpResponse<>(code, h, null);
    }

    private boolean isSuccess(int code) {
        return code >= 200 && code < 300;
    }

    private static String readAll(InputStream in) throws IOException {
        int len = Math.min(in.available(), 512);
        byte[] bs = new byte[len];
        int read = in.read(bs);
        in.close();
        return new String(bs, 0, read);
    }
}
