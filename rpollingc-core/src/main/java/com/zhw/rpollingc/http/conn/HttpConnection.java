package com.zhw.rpollingc.http.conn;

import com.zhw.rpollingc.common.RpcException;
import com.zhw.rpollingc.http.NettyConfig;
import com.zhw.rpollingc.utils.AtomicArrayCollector;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.nio.channels.ClosedChannelException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * auto reconnect http connection. <br/>
 * <p>
 * {@link NettyHttpHandler} use send request with http1.1 and keep alive the real tcp connection.
 * it will send request continuously even if the response of last request not yet received,
 * but much server will force closing connection after accepted number of http requests,
 * even if using http1.1,such as nginx(max_requests=..),tomcat.
 * so auto reconnect to the server and resend request is necessary.
 * </p>
 * <p>
 * during reconnecting this class can receive user http requests and entrust next connect to send.
 * so the best way is creating the http connection object and use it to be one of the node of cycle linked list.
 * and it will resend the requests on http pipeline when the server closed.
 * you can random to pick one connection to send request. this way also can increment the throughput
 * </p>
 */
public class HttpConnection implements HttpEndPoint, NettyHttpHandler.Listener {
    private static final HashedWheelTimer RECONNECT_TIMER = new HashedWheelTimer();
    private static final InternalLogger log = Slf4JLoggerFactory.getInstance(HttpConnection.class);

    private final Bootstrap bootstrap;
    private final long maxWaitingOpenTime;
    private final NettyConfig conf;
    private final AtomicArrayCollector<HttpRequest> waitingQueue;

    private volatile boolean userClose = false;
    HttpConnection next;
    private volatile Channel channel;
    private long lastCloseTime = -1;
    // 128个请求

    public HttpConnection(Bootstrap bootstrap, NettyConfig conf) {
        this.conf = conf;
        waitingQueue = new AtomicArrayCollector<>(conf.getMaxWaitingReSendReq());
        maxWaitingOpenTime = conf.getMaxWaitingOpenTimeMs();
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ch.pipeline().addLast("idle-handler", new IdleStateHandler(0,
                        0,
                        conf.getIdleHeartbeatInterval()));
                ch.pipeline().addLast("http-codec", new HttpResponseDecoder());
                ch.pipeline().addLast("response-body-cumulate", new HttpObjectAggregator(conf.getMaxRespBodyLen(),
                        true));
                ch.pipeline().addLast("http-pipeline-handler", new NettyHttpHandler(HttpConnection.this));
            }
        });
        this.bootstrap = bootstrap;
    }

    @Override
    public void send(HttpRequest request) throws RpcException {
        send(request, 0);
    }

    void send(HttpRequest request, int times) {
        Channel channel = this.channel;
        if (channel == null) {
            long lastCloseTime = this.lastCloseTime;
            long now = System.currentTimeMillis();

            if (userClose) {
                request.onErr(new RpcException("user close connection"));
            } else if (times < 6) {
                // 连接暂时关闭了，发送给下一个节点
                next.send(request, times + 1);
            } else if (lastCloseTime > 0L) {
                if (now - lastCloseTime > maxWaitingOpenTime) {
                    // 连接 连接已经关闭很长时间了。
                    request.onErr(new RpcException("closed connection"));
                } else {
                    // 保存到队列等待连接上了之后发送数据
                    int offer = waitingQueue.offer(request);
                    // 成功入队
                    if (offer == 0) {
                        return;
                    }
                    if (offer < 0) {
                        // 队列不是null，说明队列都满了，就不等了，直接报错
                        request.onErr(new RpcException("to many request waiting closed connection"));
                    } else {
                        // 正在collecting，此时重发就能发出去
                        send(request, times);
                    }
                }
            } else if (lastCloseTime == -2) {
                // 因为是先获取channel == null，再判断 lastCloseTime==-2.这也说明连接可能刚刚好了
                // 重新走一遍流程即可
                send(request, times);
            } else {
                // lastCloseTime==-1 连接还没打开
                request.onErr(new IllegalStateException("sending after calling connect()"));
            }
        } else {
            doWrite0(channel, request);
        }
    }

    private void doWrite0(Channel channel, HttpRequest request) {
        ByteBuf reqByteBuf = request.getReqByteBuf();
        int i = reqByteBuf.refCnt();
        if (i != 2) {
            System.out.println("-----:" + i);
        }
        // 这里必须把引用计数+1,防止发送失败被netty回收
        reqByteBuf.retain();
        ChannelFuture future = channel.writeAndFlush(request);
        future.addListener(f -> {
            if (!f.isSuccess()) {
                Throwable cause = f.cause();
                if (cause instanceof ClosedChannelException) {
                    // 调用了write 会先交给netty排队。如果排队过程中连接断开了，交接下一个节点发送
                    // 能够有机会进入netty排队，差点就发出去了，这里把times变成0，让它尽可能快的发出去
                    next.send(request, 0);
                } else if (cause != null) {
                    request.onErr(new RpcException("connection error", cause));
                } else {
                    request.onErr(new RpcException("send occur unkown error"));
                }
            }
        });

    }

    @Override
    public void connect() {
        doConnect(false);
    }

    private void doConnect(boolean asyncAndReconnect)
            throws RpcException {
        if (userClose) {
            throw new IllegalStateException("user closed");
        }
        ChannelFuture future = bootstrap.connect();
        if (asyncAndReconnect) {
            future.addListener(f -> {
                if (!f.isSuccess()) {
                    Throwable cause = f.cause();
                    String errMsg = cause != null ? cause.getMessage() : "UNKWON ERROR";
                    log.error("connect error with conf " + conf + " cause:" + errMsg);
                    long now = System.currentTimeMillis();
                    if (now - lastCloseTime > maxWaitingOpenTime) {
                        // 每次重连之前检查一下是否超过了断开连接的最大容忍时间。
                        // 超过这个时间，就把保存在队列中的请求全部返回错误。
                        Iterator<HttpRequest> iterator = waitingQueue.collect();
                        if (iterator.hasNext()) {
                            // 如果是调用了userClose,这里队列 可能是null
                            RpcException exp = new RpcException("closed connection and reconnect failed:" + errMsg);
                            for (; iterator.hasNext(); ) {
                                HttpRequest req = iterator.next();
                                req.onErr(exp);
                            }
                        }
                    } else if (userClose) {
                        Iterator<HttpRequest> iterator = waitingQueue.collect();
                        if (iterator.hasNext()) {
                            // 如果是调用了userClose,这里队列 可能是null
                            RpcException exp = new RpcException("waiting util user closed connection");
                            for (; iterator.hasNext(); ) {
                                HttpRequest req = iterator.next();
                                req.onErr(exp);
                            }
                        }
                        return;
                    }
                    scheduleReconnect();
                }
            });
        } else {
            future.awaitUninterruptibly();
            if (future.isSuccess()) {
                return;
            }
            Throwable cause = future.cause();
            if (cause != null) {
                throw new RpcException("connect failed with conf" + conf, cause);
            } else {
                throw new RpcException("unkown reason connect failed with conf" + conf);
            }
        }
    }

    @Override
    public void close() {
        userClose = true;
        Channel channel = this.channel;
        if (channel != null) {
            this.channel = null;
            channel.close();
        }
    }

    @Override
    public void onOpened(Channel ch) {
        if (userClose) {
            ch.close();
            Iterator<HttpRequest> iterator = waitingQueue.collect();
            if (iterator.hasNext()) {
                // 如果是调用了userClose,这里队列 可能是null
                RpcException exp = new RpcException("user closed connection");
                for (; iterator.hasNext(); ) {
                    HttpRequest req = iterator.next();
                    req.onErr(exp);
                }
            }
            return;
        }

        this.channel = ch;
        this.lastCloseTime = -2;
        Iterator<HttpRequest> iterator = waitingQueue.collect();
        for (; iterator.hasNext(); ) {
            doWrite0(ch, iterator.next());
        }
    }

    @Override
    public void onClosed(Collection<HttpRequest> reqs) {
        lastCloseTime = System.currentTimeMillis();
        if (userClose) {
            return;
        }
        HttpConnection next = this.next;
        if (next == null || next == this) {
            Iterator<HttpRequest> iterator = reqs.iterator();
            while (iterator.hasNext() && waitingQueue.offer(iterator.next()) < 0) ;
            if (iterator.hasNext()) {
                RpcException exp = new RpcException("to many req waiting unopened connection");
                do {
                    iterator.next().onErr(exp);
                } while (iterator.hasNext());
            }
            channel = null;
        } else {
            channel = null;
            for (HttpRequest req : reqs) {
                next.send(req);
                next = next.next;
                if (next == this) {
                    next = next.next;
                }
            }
        }
        doConnect(true);
    }

    private void scheduleReconnect() {
        if (userClose) {
            return;
        }
        RECONNECT_TIMER.newTimeout(timeout -> {
            if (!timeout.isCancelled()) {
                doConnect(true);
            }
        }, 1000, TimeUnit.MILLISECONDS);
    }
}
