package com.zhw.rpollingc.request.netty;

import io.netty.util.internal.StringUtil;

import java.net.InetSocketAddress;

public class NettyConfig {
    private String remoteHost;
    private int remotePort;
    private String hostHeader;
    private int idleHeartbeatInterval = 30;
    private long dnsExpireTime = -1;

    public NettyConfig() {
    }

    public NettyConfig(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    public InetSocketAddress getRemoteAddress() {
        return new InetSocketAddress(remoteHost, remotePort);
    }

    public void setHostHeader(String hostHeader) {
        this.hostHeader = hostHeader;
    }

    public String getRemoteHost() {
        return remoteHost;
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

    public long getDnsExpireTime() {
        return dnsExpireTime;
    }

    public void setDnsExpireTime(long dnsExpireTime) {
        this.dnsExpireTime = dnsExpireTime;
    }

    public String getHostHeader() {
        if (StringUtil.isNullOrEmpty(hostHeader)) {
            return remoteHost + ":" + remotePort;
        }
        return hostHeader;
    }

    public int getIdleHeartbeatInterval() {
        return idleHeartbeatInterval;
    }

    public void setIdleHeartbeatInterval(int idleHeartbeatInterval) {
        this.idleHeartbeatInterval = idleHeartbeatInterval;
    }

    @Override
    public String toString() {
        return "{" +
                "remoteHost='" + remoteHost + '\'' +
                ", remotePort=" + remotePort +
                ", hostHeader='" + hostHeader + '\'' +
                ", idleHeartbeatInterval=" + idleHeartbeatInterval +
                ", dnsExpireTime=" + dnsExpireTime +
                '}';
    }
}
