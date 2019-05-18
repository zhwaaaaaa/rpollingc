package com.zhw.rpollingc.request.netty;

import com.zhw.rpollingc.request.remote.Exception;

public interface NettyClient {
    void send(RequestEvent event) throws Exception;

    void schedule(Runnable runnable, long timeMs) throws Exception;

    void close();
}
