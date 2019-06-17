package com.zhw.rpollingc.promise;

@FunctionalInterface
public interface BooleanFunction<T> {

    boolean invoke(T t);

}
