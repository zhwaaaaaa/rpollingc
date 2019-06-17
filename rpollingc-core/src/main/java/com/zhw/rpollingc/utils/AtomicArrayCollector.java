package com.zhw.rpollingc.utils;

import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Consumer;

public class AtomicArrayCollector<E> {

    private static final int MARK_COLLECTING = 1 << 30;
    private static final int LEN_MARK = MARK_COLLECTING - 1;
    private static final AtomicIntegerFieldUpdater<AtomicArrayCollector> STATUS
            = AtomicIntegerFieldUpdater.newUpdater(AtomicArrayCollector.class, "status");

    @SuppressWarnings("unused")
    private volatile int status;
    private final Object[] el;

    public AtomicArrayCollector(int maxCapacity) {
        if (maxCapacity < 1 || maxCapacity > LEN_MARK) {
            throw new IllegalArgumentException("max capacity is <1 or " + LEN_MARK);
        }
        el = new Object[maxCapacity];
    }

    public int offer(E e) {
        for (; ; ) {
            int status = this.status;
            int length = status + 1;
            if (length < el.length) {
                if (STATUS.compareAndSet(this, status, length)) {
                    el[status] = e;
                    return 0;
                }
            } else {
                return length - MARK_COLLECTING;// <0 full; >0 collecting;
            }
        }
    }

    public Iterator<E> collect() {
        for (; ; ) {
            int status = this.status;
            int newStatus = status | MARK_COLLECTING;
            if (newStatus == status) {
                throw new IllegalStateException("collect only call once");
            }
            if (status == 0) {
                return Collections.emptyIterator();
            }
            if (STATUS.compareAndSet(this, status, status | newStatus)) {
                return new Itr(status);
            }
        }
    }

    private class Itr implements Iterator<E> {

        private int index = 0;
        private final int len;

        public Itr(int len) {
            this.len = len;
        }

        @Override
        public boolean hasNext() {
            return index < len;
        }

        @SuppressWarnings("unchecked")
        @Override
        public E next() {
            Object o = el[index];
            el[index++] = null;
            if (index >= len) {
                STATUS.set(AtomicArrayCollector.this, 0);
            }
            return (E) o;
        }

        @Override
        public void remove() {
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            while (hasNext()) {
                action.accept(next());
            }
        }
    }
}
