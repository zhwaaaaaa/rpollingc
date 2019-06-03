package com.zhw.rpollingc.http.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.HttpConstants;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;

public class HeadersWriter {
    final static int COLON_AND_SPACE_SHORT = (HttpConstants.COLON << 8) | HttpConstants.SP;
    final static int CRLF_SHORT = (HttpConstants.CR << 8) | HttpConstants.LF;

    private final ByteBuf buf;

    public HeadersWriter(ByteBuf buf) {
        this.buf = buf;
    }

    public void writeHeader(CharSequence name, CharSequence value) {
        final int nameLen = name.length();
        final int valueLen = value.length();
        final int entryLen = nameLen + valueLen + 4;
        buf.ensureWritable(entryLen);
        int offset = buf.writerIndex();
        writeAscii(offset, name);
        offset += nameLen;
        ByteBufUtil.setShortBE(buf, offset, COLON_AND_SPACE_SHORT);
        offset += 2;
        writeAscii(offset, value);
        offset += valueLen;
        ByteBufUtil.setShortBE(buf, offset, CRLF_SHORT);
        offset += 2;
        buf.writerIndex(offset);
    }

    void writeAscii(int offset, CharSequence value) {
        if (value instanceof AsciiString) {
            ByteBufUtil.copy((AsciiString) value, 0, buf, offset, value.length());
        } else {
            buf.setCharSequence(offset, value, CharsetUtil.US_ASCII);
        }
    }
}
