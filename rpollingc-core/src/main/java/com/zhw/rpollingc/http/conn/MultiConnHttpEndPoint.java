package com.zhw.rpollingc.http.conn;

import com.zhw.rpollingc.common.RpcException;
import com.zhw.rpollingc.http.NettyConfig;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.ThreadLocalRandom;

public class MultiConnHttpEndPoint implements HttpEndPoint {

    private static final int DEFAULT_CONN = SystemPropertyUtil.getInt("rpollingc.connection.num", 8);

    private final HttpEndPoint[] endPoints;
    private final int connNum;

    public MultiConnHttpEndPoint(NettyConfig config) {
        this(DEFAULT_CONN, config);
    }

    public MultiConnHttpEndPoint(int connNum, NettyConfig config) {
        this(connNum, new HttpConnectionFactory(config));
    }

    public MultiConnHttpEndPoint(int connNum, HttpConnectionFactory factory) {
        if (connNum < 2) {
            throw new IllegalArgumentException("connNum < 2");
        }
        this.connNum = connNum;
        HttpConnection first = factory.createConnection();
        HttpConnection t = first;

        endPoints = new HttpEndPoint[connNum];
        endPoints[0] = t;
        for (int i = 1; i < connNum; i++) {
            endPoints[i] = t.next = factory.createConnection();
            t = t.next;
        }
        t.next = first;
    }

    @Override
    public void send(HttpRequest request) throws RpcException {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int i = random.nextInt(connNum);
        endPoints[i].send(request);
    }

    @Override
    public void connect() {
        for (int i = 0; i < connNum; i++) {
            endPoints[i].connect();
        }
    }

    @Override
    public void close() {
        for (int i = 0; i < connNum; i++) {
            endPoints[i].close();
        }
    }
}
