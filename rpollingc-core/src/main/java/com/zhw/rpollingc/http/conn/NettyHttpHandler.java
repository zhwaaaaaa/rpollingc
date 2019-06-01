package com.zhw.rpollingc.http.conn;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.util.ArrayDeque;
import java.util.Collection;

public class NettyHttpHandler extends ChannelDuplexHandler {

    private final ArrayDeque<HttpRequest> sended = new ArrayDeque<>(200);

    interface Listener {
        void onOpened(Channel ch);

        void onClosed(Collection<HttpRequest> failedEvts);
    }

    private final Listener listener;

    public NettyHttpHandler(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        listener.onOpened(ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        listener.onClosed(sended);
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            ctx.write(request.getReqByteBuf(), promise)
                    .addListener(f -> {
                        if (f.isSuccess()) {
                            sended.add(request);
                        }
                    });
        }
    }
}
