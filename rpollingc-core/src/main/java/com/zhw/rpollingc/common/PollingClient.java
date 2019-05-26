package com.zhw.rpollingc.common;

import java.util.function.Consumer;

public interface PollingClient extends EndPoint {


    <R> R send(String service,
               Object msg,
               TypeReference<R> retType,
               PollingOptions options) throws RpcException;

    <R> void sendAsync(String service,
                       Object msg,
                       TypeReference<R> retType,
                       Consumer<R> onResult,
                       Consumer<Throwable> onError,
                       PollingOptions options);
}
