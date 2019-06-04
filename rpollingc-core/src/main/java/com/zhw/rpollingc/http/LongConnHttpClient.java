package com.zhw.rpollingc.http;

import com.zhw.rpollingc.common.EndPoint;
import com.zhw.rpollingc.common.RpcException;
import com.zhw.rpollingc.http.conn.HttpEndPoint;
import com.zhw.rpollingc.http.conn.HttpRequest;
import com.zhw.rpollingc.http.conn.MultiConnHttpEndPoint;
import com.zhw.rpollingc.http.protocol.HttpCodec;
import com.zhw.rpollingc.http.protocol.ReqOptions;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.ReferenceCounted;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class LongConnHttpClient implements EndPoint, HttpClient {

    private static final long timeoutMs = 10000L;

    private static void release(ReferenceCounted ref) {
        int refCnt;
        if (ref != null && (refCnt = ref.refCnt()) != 0) {
            ref.release(refCnt);
        }
    }

    class Req extends HttpRequest<ReqOptions> {
        private ByteBuf byteBuf;
        private final Consumer<FullHttpResponse> onResp;
        private final Consumer<Throwable> onErr;
        private final ReqOptions options;

        public Req(HttpMethod method, String service, Object body,
                   Consumer<FullHttpResponse> onResp,
                   Consumer<Throwable> onErr,
                   ReqOptions options) {
            super(method, service, body);
            this.onResp = onResp;
            this.onErr = onErr;
            this.options = options;
        }

        @Override
        protected ByteBuf getReqByteBuf() {
            return byteBuf;
        }

        public void setByteBuf(ByteBuf byteBuf) {
            this.byteBuf = byteBuf;
        }


        @Override
        public ReqOptions getOptions() {
            return options;
        }

        @Override
        public void onErr(Throwable err) {
            release(byteBuf);
            onErr.accept(err);
        }


        @Override
        public void onResp(FullHttpResponse response) {
            release(byteBuf);
            onResp.accept(response);
        }
    }

    private static class Res<T> {
        T res;
        Throwable err;
    }

    private HttpEndPoint endPoint;

    private final HttpCodec codec;

    public LongConnHttpClient(NettyConfig config, HttpCodec codec) {
        if (config == null || codec == null) {
            throw new NullPointerException("config||codec");
        }
        this.codec = codec;
        endPoint = new MultiConnHttpEndPoint(config);
        endPoint.connect();
    }

    @Override
    public void connect() {

    }

    @Override
    public void close() {
        endPoint.close();
    }

    private Req encodeReq(HttpMethod method, String url, Object body,
                          Consumer<FullHttpResponse> onResp,
                          Consumer<Throwable> onErr,
                          ReqOptions options) {
        Req request = new Req(method, url, body, onResp, onErr, options);
        ByteBuf byteBuf = codec.encode(request);
        request.setByteBuf(byteBuf);
        return request;
    }

    @Override
    public HttpResponse get(String url) throws RpcException {

        return sync(HttpMethod.GET, url, null, null);
    }

    private HttpResponse sync(HttpMethod method, String url, Object body, ReqOptions options) {
        Res<FullHttpResponse> res = new Res<>();

        CountDownLatch latch = new CountDownLatch(1);
        Consumer<Throwable> onErr = e -> {
            res.err = e;
            latch.countDown();
        };

        Consumer<FullHttpResponse> resp = r -> {
            res.res = r;
            latch.countDown();
        };

        Req req = encodeReq(method, url, body, resp, onErr, options);
        endPoint.send(req);

        try {
            latch.await(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RpcException(e);
        }
        if (res.err != null) {
            if (res.err instanceof RpcException) {
                throw (RpcException) res.err;
            } else {
                throw new RpcException(res.err.getMessage(), res.err);
            }
        }
        try {
            return codec.decode(res.res, req);
        } catch (RpcException e) {
            throw e;
        } catch (Throwable e) {
            throw new RpcException(e);
        } finally {
            release(res.res);
        }
    }

    @Override
    public HttpResponse post(String url, Object body) throws RpcException {
        return sync(HttpMethod.POST, url, body, null);
    }
}
