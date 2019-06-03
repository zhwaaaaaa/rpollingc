package com.zhw.rpollingc.http.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.OutputStream;

public class BodyOutputStream extends OutputStream {
    private final ByteBuf buf;
    private int size = 0;


    public BodyOutputStream(ByteBuf buf) {
        this.buf = buf;
    }

    ByteBuf getBuf() {
        return buf;
    }

    public int getSize() {
        return size;
    }

    @Override
    public void write(int b) throws IOException {
        buf.writeByte(b);
        size++;
    }

    @Override
    public void write(byte[] b) throws IOException {
        buf.writeBytes(b);
        size += b.length;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        buf.writeBytes(b, off, len);
        size += len;
    }
}
