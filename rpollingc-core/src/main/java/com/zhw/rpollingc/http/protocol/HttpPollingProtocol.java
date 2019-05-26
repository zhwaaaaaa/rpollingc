package com.zhw.rpollingc.http.protocol;

import com.zhw.rpollingc.common.PollingOptions;
import com.zhw.rpollingc.common.ProtocolException;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

public interface HttpPollingProtocol {

    FullHttpRequest createExecRequest(String service,
                                      Object body,
                                      PollingOptions options) throws ProtocolException;

    void execResponse(PollingHandler handler,
                      FullHttpResponse response,
                      PollingOptions options) throws ProtocolException;

    void pollingResponse(PollingHandler handler,
                         FullHttpResponse response,
                         PollingOptions options) throws ProtocolException;
}
