package com.zhw.rpollingc.http.conn;

import com.zhw.rpollingc.request.netty.ExpiredAddressResolverGroup;
import com.zhw.rpollingc.request.netty.NettyConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

public class HttpConnectionFactory {
    private final EventLoopGroup group;
    private NettyConfig conf;
    private Bootstrap bootstrap;

    public HttpConnectionFactory(NettyConfig conf) {
        this.group = new NioEventLoopGroup(
                Math.min(Runtime.getRuntime().availableProcessors(), 16),
                new DefaultThreadFactory("netty-", true));
        this.conf = conf;
        createBootstrap();
    }

    private void createBootstrap() {
        bootstrap = new Bootstrap();
        bootstrap.group(group);
        bootstrap.resolver(new ExpiredAddressResolverGroup(conf.getDnsExpireTime()));
        bootstrap.option(ChannelOption.AUTO_READ, true);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.remoteAddress(conf.getRemoteAddress());
    }

    public HttpConnection createConnection() {
        return new HttpConnection(bootstrap.clone(), conf);
    }
}
