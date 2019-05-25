package com.zhw.rpollingc.common;

public abstract class AbstractPollingProtocol<R> implements PollingProtocol<R> {

    @Override
    public R createRequest(String service, Object msg, TypeRefrence<?> retType) {
        return null;
    }
}
