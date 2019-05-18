package com.zhw.rpollingc.repository;

public interface RpollingcExecution {

    boolean isAsync();

    Object invoke(String url, Object body) throws Throwable;

}
