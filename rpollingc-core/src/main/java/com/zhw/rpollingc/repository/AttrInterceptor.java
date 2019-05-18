package com.zhw.rpollingc.repository;

public interface AttrInterceptor {

    Object intercept(AttrExecution execution, String url, Object body) throws Throwable;

}
