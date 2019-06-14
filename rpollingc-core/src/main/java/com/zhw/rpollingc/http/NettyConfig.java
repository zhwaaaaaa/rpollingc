package com.zhw.rpollingc.http;

import io.netty.util.internal.StringUtil;

import java.net.InetSocketAddress;

public class NettyConfig {
    private String remoteHost;
    private int remotePort = 80;
    private int idleHeartbeatInterval = 30;
    private long dnsExpireTime = -1;
    private int maxRespBodyLen = 10 << 20; // 10M
    private long maxWaitingOpenTimeMs = 5000;//5s
    private int maxWaitingReSendReq = 1000;

    public NettyConfig() {
    }

    public NettyConfig(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    public InetSocketAddress getRemoteAddress() {
        return new InetSocketAddress(remoteHost, remotePort);
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public void setMaxRespBodyLen(int maxRespBodyLen) {
        this.maxRespBodyLen = maxRespBodyLen;
    }

    public void setMaxWaitingOpenTimeMs(long maxWaitingOpenTimeMs) {
        this.maxWaitingOpenTimeMs = maxWaitingOpenTimeMs;
    }

    public long getMaxWaitingOpenTimeMs() {
        return maxWaitingOpenTimeMs;
    }

    public int getMaxRespBodyLen() {
        return maxRespBodyLen;
    }

    public long getDnsExpireTime() {
        return dnsExpireTime;
    }

    public void setDnsExpireTime(long dnsExpireTime) {
        this.dnsExpireTime = dnsExpireTime;
    }

    public int getIdleHeartbeatInterval() {
        return idleHeartbeatInterval;
    }

    public void setIdleHeartbeatInterval(int idleHeartbeatInterval) {
        this.idleHeartbeatInterval = idleHeartbeatInterval;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public int getMaxWaitingReSendReq() {
        return maxWaitingReSendReq;
    }

    public void setMaxWaitingReSendReq(int maxWaitingReSendReq) {
        this.maxWaitingReSendReq = maxWaitingReSendReq;
    }
}
