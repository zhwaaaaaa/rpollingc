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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LongConnHttpClient implements HttpClient<ReqOptions> {

    private static final long timeoutMs = 10000L;

    private static void release(ReferenceCounted ref) {
        int refCnt;
        if (ref != null && (refCnt = ref.refCnt()) != 0) {
            ref.release(refCnt);
        }
    }

    class Req extends HttpRequest<ReqOptions> {
        private ByteBuf byteBuf;
        private final BiConsumer<FullHttpResponse, Req> onResp;
        private final BiConsumer<Throwable, Req> onErr;
        private final ReqOptions options;

        public Req(HttpMethod method, String service, Object body,
                   BiConsumer<FullHttpResponse, Req> onResp,
                   BiConsumer<Throwable, Req> onErr,
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
            onErr.accept(err, this);
        }


        @Override
        public void onResp(FullHttpResponse response) {
            release(byteBuf);
            onResp.accept(response, this);
        }
    }

    private static class Res<T> {
        T res;
        Throwable err;
    }

    private HttpEndPoint endPoint;

    private final HttpCodec codec;

    private final ExecutorService executorService;

    public LongConnHttpClient(NettyConfig config, HttpCodec codec) {
        if (config == null || codec == null) {
            throw new NullPointerException("config||codec");
        }
        this.codec = codec;
        endPoint = new MultiConnHttpEndPoint(config);
        endPoint.connect();
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void connect() {

    }

    @Override
    public void close() {
        executorService.shutdown();
        endPoint.close();
    }

    private Req encodeReq(HttpMethod method, String url, Object body,
                          BiConsumer<FullHttpResponse, Req> onResp,
                          BiConsumer<Throwable, Req> onErr,
                          ReqOptions options) throws RpcException {
        Req request = new Req(method, url, body, onResp, onErr, options);
        ByteBuf byteBuf = codec.encode(request);
        request.setByteBuf(byteBuf);
        return request;
    }

    @Override
    public HttpResponse get(String url, ReqOptions options) throws RpcException {
        return sync(HttpMethod.GET, url, null, options);
    }

    private HttpResponse sync(HttpMethod method, String url, Object body, ReqOptions options) {
        Res<FullHttpResponse> res = new Res<>();

        CountDownLatch latch = new CountDownLatch(1);
        BiConsumer<Throwable, Req> onErr = (e, r) -> {
            res.err = e;
            latch.countDown();
        };

        BiConsumer<FullHttpResponse, Req> resp = (r, req) -> {
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
        if(res.res == null){
            throw new RpcException("timeout");
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

    private void async(HttpMethod method, String url, Object body,
                       Consumer<HttpResponse> onResp,
                       Consumer<Throwable> onErr, ReqOptions options) {


        BiConsumer<FullHttpResponse, Req> resp = (frs, r) -> {
            try {
                executorService.execute(() -> {
                    HttpResponse response;
                    try {
                        response = codec.decode(frs, r);
                    } catch (Throwable e) {
                        onErr.accept(e);
                        return;
                    } finally {
                        release(frs);
                    }
                    onResp.accept(response);

                });
            } catch (Throwable e) {
                release(frs);
                onErr.accept(e);
            }
        };
        BiConsumer<Throwable, Req> err = (e, r) -> onErr.accept(e);

        try {
            Req req = encodeReq(method, url, body, resp, err, options);
            endPoint.send(req);
        } catch (Throwable e) {
            onErr.accept(e);
        }
    }

    @Override
    public HttpResponse post(String url, Object body, ReqOptions options) throws RpcException {
        return sync(HttpMethod.POST, url, body, options);
    }

    @Override
    public void getAsync(String url,
                         ReqOptions options,
                         Consumer<HttpResponse> onResp,
                         Consumer<Throwable> onErr) {
        async(HttpMethod.GET, url, null, onResp, onErr, options);
    }

    @Override
    public void postAsync(String url,
                          Object body,
                          ReqOptions options,
                          Consumer<HttpResponse> onResp,
                          Consumer<Throwable> onErr) {
        async(HttpMethod.POST, url, body, onResp, onErr, options);
    }
}
