package com.zhw.rpollingc.common;

public interface PollingProtocol<R> {

    R createRequest(String service, Object msg, TypeRefrence<?> retType);

}
