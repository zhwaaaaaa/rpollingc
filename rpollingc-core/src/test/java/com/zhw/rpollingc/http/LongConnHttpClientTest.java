package com.zhw.rpollingc.http;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhw.rpollingc.common.TypeReference;
import com.zhw.rpollingc.http.protocol.HttpJsonCodec;
import com.zhw.rpollingc.http.protocol.ReqOptions;
import io.netty.buffer.PooledByteBufAllocator;

import java.util.concurrent.CountDownLatch;

public class LongConnHttpClientTest {


    @org.junit.Test
    public void close() {
    }

    @org.junit.Test
    public void get() {

        NettyConfig config = new NettyConfig();
        config.setRemoteHost("localhost");
        config.setRemotePort(3000);

        HttpJsonCodec codec = new HttpJsonCodec(new PooledByteBufAllocator(true),
                new ObjectMapper(), false, "localhost:3000");
        LongConnHttpClient client = new LongConnHttpClient(config, codec);

        ReqOptions options = new ReqOptions(TypeReference.from(Person.class));
        int num = 10000;
        CountDownLatch latch = new CountDownLatch(num);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < num; i++) {
            HttpResponse response = client.get("/json", options);
            print(response);
        }
        System.out.println("-------------" + (System.currentTimeMillis() - startTime));
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("-------------" + (System.currentTimeMillis() - startTime));
    }

    private void print(HttpResponse response) {
        Object content = response.content();
        System.out.println(content);
    }

    @org.junit.Test
    public void post() {
    }

    @org.junit.Test
    public void getAsync() {
    }

    @org.junit.Test
    public void postAsync() {
    }
}
