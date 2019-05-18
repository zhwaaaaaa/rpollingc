package com.zhw.rpollingc.request.netty;

import com.zhw.rpollingc.request.remote.ErrorResponseException;
import com.zhw.rpollingc.request.remote.Exception;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.*;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Queue;

public class NettyHttpHandler extends ChannelDuplexHandler {

    // -------debug-----------------
    private static long lastRequestId = 0L;
    private static final InternalLogger log = Slf4JLoggerFactory.getInstance(NettyHttpHandler.class);

    // -------debug-----------------

    private static final Charset charset = Charset.forName("utf-8");
    private static final String _ID_HEADER = "-Id";

    private final Queue<RequestEvent> evts = new ArrayDeque<>();
    private final String hostHeader;
    private final NettyConnection.Listener listener;

    public NettyHttpHandler(String hostHeader, NettyConnection.Listener listener) {
        this.hostHeader = hostHeader;
        this.listener = listener;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        listener.onClosed(evts);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 由于服务器返回的数据太大了。客户端解析不了，所有把这个错误扔给调用者。
        if (cause instanceof TooLongFrameException) {
            RequestEvent evt = evts.poll();
            if (evt != null) {
                evt.getOnError().accept(cause);
            }
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        listener.onOpened(ctx.channel());
        ctx.pipeline().flush();
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpResponse) {

            FullHttpResponse response = (FullHttpResponse) msg;
            try {
                RequestEvent event = evts.poll();
                if (event != null) {
                    /*String Id = response.headers().get(_ID_HEADER);
                    String uuid = String.valueOf(event.getUuid());
                    if (!Id.equals(uuid)) {
                        System.err.println(Id + "<-->" + uuid);
                    }*/
                    long receivedTime = System.currentTimeMillis();
                    log.debug("request cost {} ms ", receivedTime - event.getSentTime());

                    DecoderResult decoderResult = response.decoderResult();
                    if (decoderResult.isSuccess()) {

                        int code = response.status().code();
                        if (code >= 200 && code < 300) {
                            String s = response.content().toString(charset);
                            event.getOnResponse().accept(s);
                        } else {
                            Exception exception = new ErrorResponseException("error http code: " + code, code);
                            event.getOnError().accept(exception);
                        }
                    } else {
                        Exception exception = new ErrorResponseException("error response", decoderResult.cause());
                        event.getOnError().accept(exception);
                    }
                }
            } finally {
                response.release();
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof RequestEvent) {
            RequestEvent event = (RequestEvent) msg;
            FullHttpRequest request;
            if (msg instanceof GetEvent) {
                request = createGetRequest((GetEvent) msg);
            } else {
                request = createPostRequest((PostEvent) msg, ctx.alloc());
            }
            ctx.write(request, promise);
            long writeTime = System.currentTimeMillis();
            promise.addListener(future -> {
                if (future.isSuccess()) {
                    evts.add(event);
                    long sentTime = System.currentTimeMillis();
                    event.setSentTime(sentTime);
                    log.debug("send waiting {} ms", sentTime - writeTime);
                }
            });

        } else {
            super.write(ctx, msg, promise);
        }
    }

    private FullHttpRequest createPostRequest(PostEvent postEvent, ByteBufAllocator allocator) {
        int length = postEvent.getBody().length;
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.POST, postEvent.getUrl(), allocator.buffer(length));
        request.headers()
                .add("Host", hostHeader)
                .add("Connection", "keep-alive")
                .add("User-Agent", "mom--client(netty4)")
                .add("Content-Type", "application/json")
                .add(_ID_HEADER, postEvent.getUuid())
                .add("Content-Length", length);
        request.content().writeBytes(postEvent.getBody());
        return request;
    }

    private FullHttpRequest createGetRequest(GetEvent getEvent) {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.GET, getEvent.getUrl());
        request.headers()
                .add("Host", hostHeader)
                .add("Connection", "keep-alive")
                .add("User-Agent", "mom--client(netty4)")
                .add(_ID_HEADER, getEvent.getUuid());
        return request;
    }
}
