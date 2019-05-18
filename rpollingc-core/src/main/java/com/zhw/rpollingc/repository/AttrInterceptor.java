package com.zhw.rpollingc.repository;

public interface RpollingcInterceptor {

    Object intercept(RpollingcExecution execution, String url, Object body) throws Throwable;

}
