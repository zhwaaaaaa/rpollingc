package com.zhw.rpollingc.promise;

@FunctionalInterface
public interface Function<T, R> {

    R invoke(T t) throws Throwable;

}
