package com.zhw.rpollingc.request.netty;

import com.zhw.rpollingc.request.remote.Exception;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.ScheduledFuture;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class NettyConnection {
    private static final InternalLogger log = Slf4JLoggerFactory.getInstance(NettyConnection.class);

    private static final EventLoopGroup group = new NioEventLoopGroup(
            Math.min(Runtime.getRuntime().availableProcessors(), 16),
            new DefaultThreadFactory("netty-", true));

    private static final HashedWheelTimer RETRY_TIMER = new HashedWheelTimer();


    private static final GetEvent heartbeatEvent = new GetEvent("/heartbeat",
            e -> log.warn(" heartbeat error {}", e.getMessage()), status -> log.trace(" heartbeat status:{}", status));

    interface Listener {

        void onOpened(Channel ch);

        void onClosed(Collection<RequestEvent> failedEvts);
    }

    private class AutoReconnectHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error("Error in netty", cause);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                ctx.pipeline().writeAndFlush(heartbeatEvent);
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            if (!userClose) {
                log.debug("connection become inactive, reconnect now");
                doConnect(true);
            }
        }
    }

    private NettyConfig conf;

    private Bootstrap bootstrap;
    private volatile Channel channel;
    private volatile boolean userClose = false;

    public NettyConnection(NettyConfig conf, Listener extHandler) throws Exception {
        this.conf = conf;
        createBootstrap(extHandler);
    }

    private void createBootstrap(Listener listener) throws Exception {
        bootstrap = new Bootstrap();
        bootstrap.group(group);
        bootstrap.resolver(new ExpiredAddressResolverGroup(conf.getDnsExpireTime()));
        bootstrap.option(ChannelOption.AUTO_READ, true);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);
        bootstrap.channel(NioSocketChannel.class);

        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ch.pipeline().addLast("idle-handler", new IdleStateHandler(0, 0, conf.getIdleHeartbeatInterval()));
                ch.pipeline().addLast("http-codec", new HttpClientCodec());
                // 5M
                ch.pipeline().addLast("response-body-cumulate", new HttpObjectAggregator(10 << 20, true));
                ch.pipeline().addLast("", new NettyHttpHandler(conf.getHostHeader(), listener));
                ch.pipeline().addLast("auto-reconnect", new AutoReconnectHandler());
            }
        });
        bootstrap.remoteAddress(conf.getRemoteAddress());
        doConnect(false);
    }

    public ChannelFuture send(RequestEvent event) throws Exception {

        Channel channel = this.channel;
        if (channel == null) {
            if (userClose) {
                throw new Exception("user closed connection");
            } else {
                throw new Exception("connection not prepared");
            }
        }
        return channel.writeAndFlush(event);
    }

    public ScheduledFuture<?> schedule(Runnable runnable, long timeMs) throws Exception {

        Channel channel = this.channel;
        if (channel == null) {
            if (userClose) {
                throw new Exception("user closed connection");
            } else {
                throw new Exception("connection not prepared");
            }
        }
        return channel.eventLoop().schedule(runnable, timeMs, TimeUnit.MILLISECONDS);
    }

    private synchronized void doConnect(boolean asyncNext) throws Exception {
        if (userClose) {
            throw new IllegalStateException("user call close");
        }

        Channel oldChannel = this.channel;

        ChannelFuture channelFuture = bootstrap.connect();
        if (asyncNext) {
            channelFuture.addListener(f -> {
                if (f.isSuccess()) {
                    try {
                        doClose(oldChannel);
                    } finally {
                        this.channel = channelFuture.channel();
                        if (userClose) {
                            this.close();
                        }
                    }
                } else {
                    Throwable cause = channelFuture.cause();
                    if (cause != null) {
                        log.error("open connection failed,config:{},err:{}", conf, cause.getMessage());
                    } else {
                        log.error("open connection failed,config:{}", conf);
                    }
                    scheduleNextReconnect(1000L);
                }
            });
            return;
        }

        channelFuture.awaitUninterruptibly();

        if (channelFuture.isSuccess()) {
            Channel newChannel = channelFuture.channel();
            try {
                doClose(oldChannel);
            } finally {
                this.channel = newChannel;
            }
        } else {
            Throwable cause = channelFuture.cause();
            if (cause != null) {
                log.error("open connection with failed,config:" + conf);
                throw new Exception("connect failed", cause);
            }
        }
    }

    private void scheduleNextReconnect(long delay) {
        RETRY_TIMER.newTimeout(timeout -> {
            if (!timeout.isCancelled()) {
                try {
                    doConnect(true);
                } catch (Exception e) {
                    log.warn("connection reconnect failed, schedule next after 1000ms", e);
                    scheduleNextReconnect(1000L);
                }
            }
        }, delay, TimeUnit.MILLISECONDS);
    }


    private void doClose(Channel oldChannel) {
        if (oldChannel == null) {
            oldChannel = this.channel;
        }
        if (oldChannel != null) {
            oldChannel.close();
        }
    }

    public synchronized void close() {
        this.userClose = true;
        Channel channel = this.channel;
        this.channel = null;
        doClose(channel);
    }


}
