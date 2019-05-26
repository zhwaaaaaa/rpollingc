package com.zhw.rpollingc.http;

import com.zhw.rpollingc.common.*;
import com.zhw.rpollingc.http.conn.HttpEndPoint;
import com.zhw.rpollingc.http.conn.HttpRequest;
import com.zhw.rpollingc.http.conn.MultiConnClient;
import com.zhw.rpollingc.http.protocol.HttpPollingProtocol;
import com.zhw.rpollingc.http.protocol.PollingHandler;
import com.zhw.rpollingc.request.netty.NettyConfig;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.HashedWheelTimer;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class HttpPollingClient implements PollingClient {

    private abstract class HttpRequestAdapter<R> extends HttpRequest implements PollingHandler<R> {
        Consumer<Throwable> onErr;
        Consumer<R> onResult;
        PollingOptions options;

        @Override
        public void onResp(FullHttpResponse resp, HttpEndPoint endPoint) {
            DecoderResult result = resp.decoderResult();
            if (result.isSuccess()) {
                try {
                    dispatchProtocol(resp, options);
                } catch (ProtocolException e) {
                    onErr.accept(e);
                }
            } else {
                Throwable cause = result.cause();
                if (cause == null) {
                    onErr.accept(new ErrorResposeException("error response from server"));
                    return;
                }
                onErr.accept(new ErrorResposeException("error response from server", cause));
            }
        }

        protected abstract void dispatchProtocol(FullHttpResponse resp,
                                                 PollingOptions options) throws ProtocolException;

        @Override
        public void onError(Throwable err, HttpEndPoint endPoint) {
            onErr.accept(new ErrorResposeException("error response from server", err));
        }

        @Override
        public HttpEndPoint getHttpEndPoint() {
            return endPoint;
        }

        @Override
        public void schedulePolling(Callable<FullHttpRequest> onTimeout, int timeoutMs) {
            pollingTimer.newTimeout(timeout -> {
                try {
                    FullHttpRequest request = onTimeout.call();
                    PollingRequest<R> pr = new PollingRequest<>();
                    pr.setFullHttpRequest(request);
                    pr.onErr = this.onErr;
                    pr.options = options;
                    pr.onResult = onResult;
                    endPoint.send(this);
                } catch (RpcException err) {
                    this.onErr.accept(err);
                }
            }, timeoutMs, TimeUnit.MILLISECONDS);
        }

        @Override
        public void handleResult(R r) {
            onResult.accept(r);
        }
    }

    private class ExecRequest<R> extends HttpRequestAdapter<R> {

        @Override
        protected void dispatchProtocol(FullHttpResponse resp,
                                        PollingOptions options) throws ProtocolException {
            protocol.execResponse(this, resp, options);
        }
    }

    private class PollingRequest<R> extends HttpRequestAdapter<R> {

        @Override
        protected void dispatchProtocol(FullHttpResponse resp,
                                        PollingOptions options) throws ProtocolException {
            protocol.pollingReponse(this, resp, options);
        }
    }

    private static class Res<T> {
        T res;
        Throwable err;
    }

    private HttpEndPoint endPoint;
    private HttpPollingProtocol protocol;
    private HashedWheelTimer pollingTimer = new HashedWheelTimer();


    public HttpPollingClient(NettyConfig config, HttpPollingProtocol protocol) {
        if (config == null) {
            throw new NullPointerException("config");
        }
        endPoint = new MultiConnClient(config);
        this.protocol = protocol;
    }

    @Override
    public void connect() {
        endPoint.connect();
    }

    @Override
    public void close() {
        endPoint.close();
    }

    @Override
    public <R> R send(String service,
                      Object msg,
                      TypeReference<R> retType,
                      PollingOptions options) throws RpcException {

        FullHttpRequest request = protocol.createExecRequest(service, msg, options);

        ExecRequest<R> req = new ExecRequest<>();
        req.setFullHttpRequest(request);
        req.options = options;

        Res<R> r = new Res<>();
        CountDownLatch latch = new CountDownLatch(1);
        req.onErr = e -> {
            r.err = e;
            latch.countDown();
        };
        req.onResult = res -> {
            r.res = res;
            latch.countDown();
        };
        endPoint.send(req);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RpcException("request interrupted", e);
        }
        if (r.err != null) {
            throw r.err instanceof RpcException ? (RpcException) r.err
                    : new RpcException("unknown error", r.err);
        }
        return r.res;
    }

    @Override
    public <R> void sendAsync(String service,
                              Object msg,
                              TypeReference<R> retType,
                              Consumer<R> onResult,
                              Consumer<Throwable> onError,
                              PollingOptions options) {

    }
}
