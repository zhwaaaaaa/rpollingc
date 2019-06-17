package com.zhw.rpollingc.promise;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public abstract class AbstractPromise<T> implements Promise<T>, Observer<T> {

    @SuppressWarnings("unchecked")
    private static final ObNode HEAD_HOLDER = new ObNode(null);

    private static final AtomicReferenceFieldUpdater<AbstractPromise, ObNode> TAIL
            = AtomicReferenceFieldUpdater.newUpdater(AbstractPromise.class, ObNode.class, "tail");


    private static final int STATE_PENDING = 0;
    private static final int STATE_REJECT = 1;
    private static final int STATE_RESOLVE = 2;
    private volatile ObNode<T> head;
    @SuppressWarnings("unchecked")
    private volatile ObNode<T> tail = HEAD_HOLDER;

    private T r;
    private Throwable e;

    protected final void addObserver(Observer<T> observer) {
        ObNode<T> node = new ObNode<>(observer);

        for (; ; ) {
            ObNode<T> tail = this.tail;
            if (tail == null) {
                invokeObserver(node);
                return;
            } else if (tail == HEAD_HOLDER) {
                if (TAIL.compareAndSet(this, tail, node)) {
                    this.head = node;
                    break;
                }
            } else if (TAIL.compareAndSet(this, tail, node)) {
                tail.next = node;
                break;
            }
        }
        ObNode<T> tail = this.tail;
        if (tail == null) {
            invokeObserver(node);
        }
    }

    private void invokeObserver(Observer<T> observer) {
        if (e == null && r == null) {
            throw new IllegalStateException("not notifyed");
        }
        if (e != null) {
            observer.notifyErr(e);
        } else {
            try {
                observer.notify(r);
            } catch (Throwable e) {
                observer.notifyErr(e);
            }
        }
    }

    @Override
    public void notify(T t) {
        this.r = t;
        notify0();
    }

    @Override
    public void notifyErr(Throwable t) {
        this.e = t;
        notify0();
    }

    private void notify0() {
        for (; ; ) {
            ObNode<T> tail = this.tail;
            if (tail == null) {
                throw new IllegalStateException("notify twice");
            } else if (TAIL.compareAndSet(this, tail, null)) {
                if (tail != HEAD_HOLDER) {
                    for (; ; ) {
                        ObNode<T> head = this.head;
                        if (head != null) {
                            this.head = null;
                            do {
                                invokeObserver(head);
                                head = head.next;
                            } while (head != null);
                            break;
                        }
                    }
                }
                return;
            }
        }
    }


    private static class ObNode<T> implements Observer<T> {

        static final AtomicIntegerFieldUpdater<ObNode> ST
                = AtomicIntegerFieldUpdater.newUpdater(ObNode.class, "state");

        final Observer<T> observer;
        volatile int state = STATE_PENDING;
        volatile ObNode<T> next;


        public ObNode(Observer<T> observer) {
            this.observer = observer;
        }


        @Override
        public void notify(T t) {
            int state = this.state;
            if (state == STATE_PENDING && ST.compareAndSet(this, state, STATE_RESOLVE)) {
                try {
                    observer.notify(t);
                } catch (Throwable e) {
                    observer.notifyErr(e);
                }
            }
        }

        @Override
        public void notifyErr(Throwable t) {
            int state = this.state;
            if (state == STATE_PENDING && ST.compareAndSet(this, state, STATE_REJECT)) {
                try {
                    observer.notifyErr(t);
                } catch (Throwable ignore) {
                    // ignore
                }
            }
        }
    }
}
