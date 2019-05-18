package com.zhw.rpollingc.repository;

public interface AttrExecution {

    boolean isAsync();

    Object invoke(String url, Object body) throws Throwable;

}
