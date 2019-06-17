package com.zhw.rpollingc.promise;

import java.util.concurrent.TimeUnit;

public interface Promise<T> {

    <R> Promise<R> then(Function<T, R> func);

    <R> Promise<R> then(PromiseFunction<T, R> func);

    <R, E extends Throwable> Promise<R> catchErr(Class<E> clz, Function<E, R> func);

    <R, E extends Throwable> Promise<R> catchErr(Class<E> clz, PromiseFunction<E, R> func);

    <R> Promise<R> util(Function<T, R> func, BooleanFunction<R> util);

    <R> Promise<R> util(PromiseFunction<T, R> func, BooleanFunction<R> util);

    Promise<T> timeout(long timeout, TimeUnit timeUnit);

}
