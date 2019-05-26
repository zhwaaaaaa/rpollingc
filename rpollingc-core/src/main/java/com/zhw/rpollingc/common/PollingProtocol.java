package com.zhw.rpollingc.common;

import java.util.function.Consumer;

public interface PollingProtocol<R, T> {

    R createRequest(String service,
                    Object msg,
                    TypeReference<T> retType,
                    Consumer<T> onResult,
                    Consumer<Throwable> onError,
                    PollingOptions options) throws ProtocolException;

}
