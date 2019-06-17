package com.zhw.rpollingc.polling;

import com.zhw.rpollingc.common.TypeReference;

public interface PollingHandler<C, E extends PollingExecutor<C>> {

    void handle(E executor, String service, Object param, TypeReference typeReference, long timeoutMs);

}
