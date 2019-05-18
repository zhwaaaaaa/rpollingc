package com.zhw.rpollingc.request.netty;

import com.zhw.rpollingc.request.protocol.QueryResultExecutor;
import com.zhw.rpollingc.request.remote.ClientException;
import com.zhw.rpollingc.request.remote.ErrorResponseException;
import com.zhw.rpollingc.request.remote.Exception;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.io.IOException;
import java.util.function.Consumer;

public class NettyHttpExecutor extends QueryResultExecutor {

    static {
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
    }

    private final NettyClient client;

    private ObjectMapper requestMapper;
    private ObjectMapper responseMapper;

    public NettyHttpExecutor(NettyConfig conf) throws Exception {
        this(conf, new ObjectMapper(), new ObjectMapper());
    }

    public NettyHttpExecutor(NettyConfig conf,
                             ObjectMapper requestMapper,
                             ObjectMapper responseMapper) throws Exception {
        this.requestMapper = requestMapper;
        this.responseMapper = responseMapper;
        client = new MultiConnClient(conf);

    }

    public NettyHttpExecutor(NettyConfig conf, int queryIntervalMs,
                             ObjectMapper requestMapper, ObjectMapper responseMapper) throws Exception {
        super(queryIntervalMs);
        client = new MultiConnClient(conf);
        this.requestMapper = requestMapper;
        this.responseMapper = responseMapper;
    }

    @Override
    public <T> void scheduleGet(String url, Consumer<T> onSuccess,
                                Consumer<Throwable> onError,
                                TypeReference<T> resultClass, long timeoutMs) throws Exception {
        client.schedule(() -> {
            try {
                get(url, onSuccess, onError, resultClass);
            } catch (Exception e) {
                onError.accept(e);
            }
        }, timeoutMs);
    }

    @Override
    public <T> void get(String url,
                        Consumer<T> onSuccess,
                        Consumer<Throwable> onError,
                        TypeReference<T> resultClass) throws Exception {

        GetEvent event = new GetEvent(url, onError, str -> {
            try {
                T o = responseMapper.readValue(str, resultClass);
                onSuccess.accept(o);
            } catch (IOException e) {
                onError.accept(new ErrorResponseException(e));
            }
        });
        client.send(event);

    }

    @Override
    public <T> void post(String url,
                         Object body,
                         Consumer<T> onSuccess,
                         Consumer<Throwable> onError,
                         TypeReference<T> resultClass) throws Exception {
        try {
            byte[] bytes = requestMapper.writeValueAsBytes(body);
            PostEvent event = new PostEvent(url, onError, str -> {
                try {
                    T o = responseMapper.readValue(str, resultClass);
                    onSuccess.accept(o);
                } catch (IOException e) {
                    onError.accept(new ErrorResponseException(e));
                }
            }, bytes);
            client.send(event);
        } catch (JsonProcessingException e) {
            throw new ClientException("serializer body occur error", e);
        }
    }

    @Override
    public <T> void schedulePost(String url, Object body,
                                 Consumer<T> onSuccess,
                                 Consumer<Throwable> onError,
                                 TypeReference<T> resultClass,
                                 long timeoutMs) throws Exception {
        client.schedule(() -> {
            try {
                post(url, body, onSuccess, onError, resultClass);
            } catch (Exception e) {
                onError.accept(e);
            }
        }, timeoutMs);
    }

    public void close() {
        client.close();
    }
}
