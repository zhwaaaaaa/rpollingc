package com.zhw.rpollingc.http.protocol;

import com.zhw.rpollingc.http.conn.HttpEndPoint;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.concurrent.Callable;

public interface PollingHandler<R> {

    HttpEndPoint getHttpEndPoint();

    void schedulePolling(Callable<FullHttpRequest> onTimeout, int timeoutMs);

    void handleResult(R r);

}
