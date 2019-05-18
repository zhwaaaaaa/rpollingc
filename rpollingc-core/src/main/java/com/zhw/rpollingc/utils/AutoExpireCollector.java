package com.zhw.rpollingc.utils;

import io.netty.util.HashedWheelTimer;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class AutoExpireCollector<T> {

    private static class Node<T> {
        volatile Node next;
        T t;

        Node(T t) {
            this.t = t;
        }
    }

    private static final AtomicReferenceFieldUpdater<Node, Node> UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "next");
    private static final AtomicReferenceFieldUpdater<AutoExpireCollector, Node> UPDATER_TAIL =
            AtomicReferenceFieldUpdater.newUpdater(AutoExpireCollector.class, Node.class, "tail");

    private final int expireTime;
    private final HashedWheelTimer timer;

    private volatile Node<T> head = null;
    private volatile Node<T> tail = null;


    public AutoExpireCollector(int expireTime) {
        this.expireTime = expireTime;
        this.timer = new HashedWheelTimer();
    }

    public AutoExpireCollector(int expireTime, HashedWheelTimer timer) {
        this.expireTime = expireTime;
        this.timer = timer;
    }

    public void push(T v) {
        Node<T> node = new Node<>(v);
        if (this.tail == null) {
            if (UPDATER_TAIL.compareAndSet(this, null, node)) {
                head = node;
                return;
            }
        }
        for (; ; ) {
            Node<T> tail = this.tail;
            if (UPDATER.compareAndSet(tail, null, node)) {
                this.tail = node;
                return;
            }
        }
    }

    public List<T> collect() {
        Node<T> tail = this.tail;

        if (tail == null) {
            return Collections.emptyList();
        }

        for (; ; ) {
            Node<T> head = this.head;
            tail = this.tail;
            if (UPDATER_TAIL.compareAndSet(this, tail, null)) {
                break;
            }
        }

        return Collections.emptyList();
    }

}
