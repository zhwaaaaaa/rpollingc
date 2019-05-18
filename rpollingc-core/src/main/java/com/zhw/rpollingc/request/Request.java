package com.zhw.rpollingc.request;


import com.zhw.rpollingc.request.remote.Exception;
import com.fasterxml.jackson.core.type.TypeReference;
import io.reactivex.Observable;

public interface Request {

    String url();

    Request url(String url);

    Object body();

    Request body(Object body);

    int timeout();

    Request timeout(int timeoutSecond);

    <T> T send(TypeReference<T> resultType) throws Throwable;

    <T> Observable<T> sendAsync(TypeReference<T> resultType) throws Exception;

}
