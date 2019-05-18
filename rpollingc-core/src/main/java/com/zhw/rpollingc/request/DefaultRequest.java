package com.zhw.rpollingc.request;

import com.zhw.rpollingc.request.protocol.ResultDecoder;
import com.zhw.rpollingc.request.remote.ErrorResponseException;
import com.zhw.rpollingc.request.remote.Exception;
import com.zhw.rpollingc.request.remote.TimeoutException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.reactivex.Observable;
import io.reactivex.plugins.RxJavaPlugins;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DefaultRequest implements Request {
    private static final InternalLogger log = InternalLoggerFactory.getInstance(DefaultRequest.class);

    static {
        RxJavaPlugins.setErrorHandler(log::error);
    }

    private String url;
    private Object body;
    private ExecutorService resultThreadPool;
    private RequestExecutor executor;
    private int timeoutMs;

    private ResultDecoder resultDecoder;

    public DefaultRequest(RequestExecutor executor, ResultDecoder resultDecoder) {
        this(null, executor, resultDecoder);
    }

    public DefaultRequest(ExecutorService resultThreadPool,
                                RequestExecutor executor,
                                ResultDecoder resultDecoder) {
        if (resultThreadPool == null) {
            resultThreadPool = Executors.newFixedThreadPool(1);
        }
        if (resultDecoder == null) {
            throw new NullPointerException("resultDecoder");
        }
        this.resultThreadPool = resultThreadPool;
        this.executor = executor;
        this.resultDecoder = resultDecoder;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public Request url(String url) {
        this.url = url;
        return this;
    }

    @Override
    public Object body() {
        return body;
    }

    @Override
    public Request body(Object body) {
        this.body = body;
        return this;
    }

    @Override
    public int timeout() {
        return timeoutMs / 1000;
    }

    @Override
    public Request timeout(int timeoutSecond) {
        this.timeoutMs = timeoutSecond * 1000;
        return this;
    }

    private static class ResErr {
        String res;
        Throwable err;
    }

    @Override
    public <T> T send(TypeReference<T> resultType) throws Throwable {
        CountDownLatch latch = new CountDownLatch(1);
        ResErr resErr = new ResErr();
        executor.submitRequest(this.url,
                this.body,
                result -> {
                    resErr.res = result;
                    latch.countDown();
                },
                err -> {
                    resErr.err = err;
                    latch.countDown();
                },
                timeoutMs);
        if (latch.await(timeoutMs + 2000, TimeUnit.MILLISECONDS)) {
            if (resErr.err != null) {
                throw resErr.err;
            }
            T decode = resultDecoder.decode(resErr.res, resultType);
            assertNotNull(decode);
            return decode;
        } else {
            throw new TimeoutException("waiting timeout");
        }
    }

    private static void assertNotNull(Object o) throws ErrorResponseException {
        if (o == null) {
            throw new ErrorResponseException("result is null");
        }
    }

    @Override
    public <T> Observable<T> sendAsync(TypeReference<T> resultType) throws Exception {
        ResultSubject<T> resultSubject = new ResultSubject<>();

        Consumer<String> notifyResult = t -> resultThreadPool.execute(() -> {
            try {
                T decode = resultDecoder.decode(t, resultType);
                assertNotNull(decode);
                resultSubject.notifyResult(decode);
            } catch (Exception e) {
                resultSubject.notifyError(e);
            }
        });

        Consumer<Throwable> notifyError = err -> resultThreadPool.execute(() -> resultSubject.notifyError(err));
        executor.submitRequest(this.url,
                this.body,
                notifyResult,
                notifyError,
                timeoutMs);

        return resultSubject;
    }
}

