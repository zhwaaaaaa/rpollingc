package com.zhw.rpollingc.http.netty;

import io.netty.resolver.*;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class ExpiredAddressResolverGroup extends AddressResolverGroup<InetSocketAddress> {
    private long expireTimeMs;

    public ExpiredAddressResolverGroup(long expireTimeMs) {
        this.expireTimeMs = expireTimeMs;
    }

    @Override
    protected AddressResolver<InetSocketAddress> newResolver(EventExecutor executor) throws Exception {
        return new ExpiredNotValidResolver(executor, new DefaultNameResolver(executor), expireTimeMs);
    }
}

class ExpiredNotValidResolver extends InetSocketAddressResolver {
    private volatile long lastResolveTime;
    private final long expireTimeMs;

    public ExpiredNotValidResolver(EventExecutor executor,
                                   NameResolver<InetAddress> nameResolver,
                                   long expireTimeMs) {
        super(executor, nameResolver);
        this.expireTimeMs = expireTimeMs;
    }

    @Override
    protected boolean doIsResolved(InetSocketAddress address) {
        // 超过时间了则要重新解析,防止rancher等容器重新部署更改IP地址
        if (expireTimeMs != -1 && System.currentTimeMillis() - lastResolveTime > expireTimeMs) {
            return false;
        }
        return super.doIsResolved(address);
    }

    @Override
    protected void doResolve(InetSocketAddress unresolvedAddress, Promise<InetSocketAddress> promise) throws Exception {
        promise.addListener(future -> {
            if (future.isSuccess()) {
                lastResolveTime = System.currentTimeMillis();
            }
        });
        super.doResolve(unresolvedAddress, promise);
    }
}
