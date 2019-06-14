package com.zhw.rpollingc.polling;

import com.zhw.rpollingc.common.ReflectTypeReference;
import com.zhw.rpollingc.common.RpcException;

import java.util.function.Consumer;

public interface PollingClient {

    <T> T send(String url, Object body, ReflectTypeReference<T> typeReference, int maxWaitingMs) throws RpcException;

    <T> void sendAsync(String url, Object body, ReflectTypeReference<T> typeReference,
                       Consumer<T> onResult, Consumer<RpcException> onError, int maxWaitingMs);

}
