package com.zhw.rpollingc.promise;

public interface Observer<T> {

    void notify(T t);

    void notifyErr(Throwable t);

}
