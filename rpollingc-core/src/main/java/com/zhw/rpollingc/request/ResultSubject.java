package com.zhw.rpollingc.request;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.subjects.BehaviorSubject;

import java.util.concurrent.atomic.AtomicBoolean;


public class ResultSubject<T> extends Observable<T> {

    private final BehaviorSubject<T> source = BehaviorSubject.create();

    private AtomicBoolean completed = new AtomicBoolean(false);
    private T result;
    private Throwable error;

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        if (completed.get()) {
            if (error != null) {
                observer.onError(error);
            } else {
                observer.onNext(result);
                observer.onComplete();
            }
        } else {
            source.subscribe(observer);
        }
    }


    public void notifyError(Throwable error) {
        this.error = error;
        if (completed.compareAndSet(false, true)) {
            source.onError(error);
        } else {
            throw new IllegalStateException("result notified");
        }
    }

    public void notifyResult(T result) {
        this.result = result;
        if (completed.compareAndSet(false, true)) {
            source.onNext(result);
            source.onComplete();
        } else {
            throw new IllegalStateException("result notified");
        }
    }


}
