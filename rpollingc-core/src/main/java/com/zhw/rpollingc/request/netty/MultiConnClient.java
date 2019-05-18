package com.zhw.rpollingc.request.netty;

import com.zhw.rpollingc.request.remote.Exception;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.concurrent.ScheduledFuture;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Netty多连接客户端，连接选取采用随机方法。
 */
public class MultiConnClient implements NettyClient {

    private static final int RETRY = 10;
    private static final int DEFAULT_CONN = 6;

    private static final InternalLogger log = Slf4JLoggerFactory.getInstance(MultiConnClient.class);
    private static final HashedWheelTimer waitingSendedTimer = new HashedWheelTimer();

    class Client implements NettyConnection.Listener {

        private final NettyConnection conn;
        private Client next;

        private volatile Collection<RequestEvent> openSended;

        private Collection<RequestEvent> waitingSended = new ArrayList<>();

        private final ReentrantLock lock = new ReentrantLock();

        private volatile long disconnectedTime = -1L;

        @Override
        public void onOpened(Channel ch) {
            Collection<RequestEvent> sended = this.openSended;
            this.openSended = null;
            if (sended != null) {
                for (RequestEvent event : sended) {
                    ch.writeAndFlush(event);
                }
            }
            // 发送之前等待的数据
            ReentrantLock lock = this.lock;
            lock.lock();
            try {
                Collection<RequestEvent> waiting = this.waitingSended;
                this.waitingSended = null;
                if (waiting != null) {
                    for (RequestEvent event : waiting) {
                        ch.writeAndFlush(event);
                    }
                }
            } finally {
                lock.unlock();
            }
            this.disconnectedTime = -1;
        }

        @Override
        public void onClosed(Collection<RequestEvent> failedEvts) {
            this.disconnectedTime = System.currentTimeMillis();

            int size = failedEvts.size();
            if (size == 0) {
                return;
            }

            if (clientSize == 1) {
                this.openSended = failedEvts;
                return;
            }

            Client t = next;

            for (RequestEvent evt : failedEvts) {
                if (t == this) {
                    t = t.next;
                }
                try {
                    t.send0(evt, 0);
                } catch (Exception e) {
                    evt.getOnError().accept(e);
                }
                t = t.next;
            }

        }

        public Client(NettyConfig conf) throws Exception {
            this.conn = new NettyConnection(conf, this);
        }


        public void send0(RequestEvent event, int num) throws Exception {
            long disconnectedTime = this.disconnectedTime;
            if (disconnectedTime > 0L) {
                if (System.currentTimeMillis() - disconnectedTime > 5000L) {
                    // 连接关闭时间 > 5s 说明已经无法使用
                    event.getOnError().accept(new Exception("closed connection"));
                } else {
                    // 当前正在重连，给下一个连接发送
                    nextResend(event, num);
                }
                return;
            }
            ChannelFuture future = conn.send(event);
            future.addListener(f -> {
                if (!f.isSuccess()) {
                    Throwable cause = f.cause();
                    if (cause instanceof ClosedChannelException) {
                        // 已经扔给netty之后连接关闭了
                        nextResend(event, num);
                    } else if (cause != null) {
                        event.getOnError().accept(cause);
                    } else {
                        log.warn("send to  with unkownn err {}", event);
                    }
                }
            });
        }

        public void nextResend(RequestEvent event, int num) {
            if (num > RETRY) {
                ReentrantLock lock = this.lock;
                lock.lock();
                try {
                    long disconnectedTime = this.disconnectedTime;
                    long now = System.currentTimeMillis();
                    if (disconnectedTime == -1L) {
                        try {
                            send0(event, 0);
                        } catch (Exception e) {
                            event.getOnError().accept(e);
                        }
                    } else {
                        long l1 = now - disconnectedTime;
                        if (l1 < 5000L) {
                            Collection<RequestEvent> waiting = this.waitingSended;
                            if (waiting == null) {
                                Collection<RequestEvent> newArr = waiting = new ArrayList<>();
                                this.waitingSended = waiting;
                                errIfNotSend(l1, newArr);
                            }
                            waiting.add(event);
                        } else {
                            event.getOnError().accept(new Exception("closed connection"));
                        }
                    }

                } finally {
                    lock.unlock();
                }
            } else {
                try {
                    next.send0(event, num + 1);
                } catch (Exception e) {
                    event.getOnError().accept(e);
                }
            }
        }

        private void errIfNotSend(long l1, Collection<RequestEvent> newArr) {
            waitingSendedTimer.newTimeout((Timeout timeout) -> {
                if (timeout.cancel()) {
                    ReentrantLock l = this.lock;
                    l.lock();
                    try {
                        if (newArr == this.waitingSended) {
                            Exception exception = new Exception("closed connection");
                            for (RequestEvent requestEvent : newArr) {
                                requestEvent.getOnError().accept(exception);
                            }
                        }
                    } finally {
                        l.unlock();
                    }
                }
            }, l1, TimeUnit.MILLISECONDS);
        }

        public ScheduledFuture<?> schedule(Runnable runnable, long timeMs) throws Exception {
            return conn.schedule(runnable, timeMs);
        }

    }

    private final int clientSize;
    private Client[] clients;

    public MultiConnClient(NettyConfig conf) throws Exception {
        this(DEFAULT_CONN, conf);
    }

    public MultiConnClient(int clientSize, NettyConfig conf) throws Exception {
        Client head = new Client(conf);
        clientSize = this.clientSize = clientSize < 2 ? 1 : clientSize;
        int size = 0;
        if (clientSize < 2) {
            clients = new Client[1];
            clients[0] = head;
            return;
        }
        clients = new Client[clientSize];
        clients[0] = head;

        Client tail = head;
        while (++size < clientSize) {
            tail = tail.next = new Client(conf);
            clients[size] = tail;
        }
        tail.next = head;
    }


    @Override
    public void send(RequestEvent event) throws Exception {
        //随机算法。
        int i = ThreadLocalRandom.current().nextInt(clientSize);
        clients[i].send0(event, 0);
    }

    @Override
    public void schedule(Runnable runnable, long timeMs) throws Exception {
        //随机算法。
        int i = ThreadLocalRandom.current().nextInt(clientSize);
        clients[i].schedule(runnable, timeMs);
    }

    @Override
    public void close() {
        for (Client client : clients) {
            client.conn.close();
        }
    }
}
