package com.zhw.rpollingc.http.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.Collections;
import java.util.List;

public class BodyAllocator {
    private final ByteBufAllocator allocator;
    private List<ByteBuf> bufs = Collections.emptyList();

    public BodyAllocator(ByteBufAllocator allocator) {
        this.allocator = allocator;
    }

    public BodyOutputStream getBodyOutputStream() {
        // 一般只调用一次
        ByteBuf buffer = allocator.buffer();
        bufs = Collections.singletonList(buffer);
        return new BodyOutputStream(buffer);
    }

    List<ByteBuf> getBufs() {
        return bufs;
    }
}
